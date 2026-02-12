package com.bootcamp.paymentproject.payment.service;

import com.bootcamp.paymentproject.order.Repository.OrderProductRepository;
import com.bootcamp.paymentproject.order.Repository.OrderRepository;
import com.bootcamp.paymentproject.order.dto.OrderProductQuantityDto;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.payment.dto.request.CreatePaymentRequest;
import com.bootcamp.paymentproject.payment.dto.response.ConfirmPaymentResponse;
import com.bootcamp.paymentproject.payment.dto.response.CreatePaymentResponse;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import com.bootcamp.paymentproject.payment.exception.PaymentNotFoundException;
import com.bootcamp.paymentproject.payment.exception.PointInsufficientException;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new IllegalStateException("해당 주문을 찾지 못했습니다.")
        );

        BigDecimal remainingPoint = pointTransactionRepository.getPointSumByUserId(order.getUser().getId(), PointType.EARN);

        if(request.getPointsToUse().compareTo(remainingPoint) > 0) {
            throw new PointInsufficientException();
        }

        String paymentId = "payment-" + order.getId() + "-" +LocalDateTime.now();

        // 총 결제금액은 상품 가격의 총 합 - 사용할 포인트로 계산
        Payment payment = new Payment(paymentId, request.getTotalAmount().subtract(request.getPointsToUse()), order);
        paymentRepository.save(payment);

        order.updatePointToUse(request.getPointsToUse());

        return CreatePaymentResponse.fromEntity(payment);
    }

    @Transactional
    public ConfirmPaymentResponse confirmPaymentTransaction(String paymentId, PortOnePaymentResponse portOnePayment) {

        Payment dbPayment = paymentRepository.findByPaymentId(paymentId).orElseThrow(
                PaymentNotFoundException::new
        );

        // 멱등성 검사
        if(dbPayment.getStatus() != PaymentStatus.PENDING) {
            return ConfirmPaymentResponse.fromEntityWithMessage(dbPayment,false,"이미 처리 완료된 결제입니다.");
        }

        // 실제 결제가 성공했는지 검사
        if(!portOnePayment.isPaid()) {
            dbPayment.paymentFailed();

            return ConfirmPaymentResponse.fromEntityWithMessage(dbPayment, false, "실패로 처리된 결제입니다.");
        }

        // 주문 금액과 실결제 금액이 다를 때 환불 프로세스 진행
        if((dbPayment.getAmount().compareTo(portOnePayment.amount().total())) != 0) {
            return ConfirmPaymentResponse.fromEntityWithMessage(dbPayment, true,"결제 금액이 상이하여 결제가 취소처리 되었습니다.");
        }

        // 재고 확인 및 차감 단계
        List<OrderProductQuantityDto> byOrderId = orderProductRepository.findByOrderId(dbPayment.getOrder().getId());

        List<Product> OrderedProductList = productRepository.findAllByIdIn(
                byOrderId.stream()
                .map(OrderProductQuantityDto::getProductId)
                .toList()
        );

        // 상품 가격이 변경되어 주문-상품 테이블에 추가될 때
        // 상품id 중복이 가능하므로 key가 겹치면 value를 합침
        Map<Long, Long> productMap = byOrderId.stream()
                .collect(Collectors.toMap(
                        OrderProductQuantityDto::getProductId,
                        OrderProductQuantityDto::getQuantity,
                        Long::sum
                ));

        // 주문한 상품 목록이 db에 저장된 상품 목록과 맞지 않을 때 환불 프로세스 진행
        if(byOrderId.size() != OrderedProductList.size()) {
            return ConfirmPaymentResponse.fromEntityWithMessage(dbPayment, true,"주문 상품 정보가 일치하지 않아 결제가 취소되었습니다.");
        }

        // 주문한 상품 수량이 db에 저장된 상품 재고보다 많을 때 환불 프로세스 진행
        for(Product p : OrderedProductList) {
            if(p.getStock() < productMap.get(p.getId())) {

                return ConfirmPaymentResponse.fromEntityWithMessage(dbPayment, true,"상품의 재고가 부족하여 결제가 취소되었습니다.");
            } else {
                p.decreaseStock(productMap.get(p.getId()));
            }
        }

        dbPayment.paymentConfirmed();

        Order order = dbPayment.getOrder();
        order.orderCompleted();

        return ConfirmPaymentResponse.fromEntityWithMessage(dbPayment, false,"결제 확인이 성공적으로 완료되었습니다.");
    }
}

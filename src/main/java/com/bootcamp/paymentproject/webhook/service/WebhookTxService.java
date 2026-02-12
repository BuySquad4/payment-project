package com.bootcamp.paymentproject.webhook.service;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.entity.OrderProduct;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import com.bootcamp.paymentproject.webhook.dto.PortoneWebhookPayload;
import com.bootcamp.paymentproject.webhook.entity.WebhookEvent;
import com.bootcamp.paymentproject.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookTxService {

    private final WebhookEventRepository webhookEventRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;

    /**
     * PortOne webhook DB 반영 처리 (트랜잭션)
     *
     * 흐름:
     * 1) webhook_event 저장 (멱등 처리)
     * 2) Payment / Order 조회
     * 3) 결제 상태 검증 및 상태 변경
     * 4) 재고 차감 (결제 승인 시)
     * 5) webhook_event 상태를 PROCESSED 또는 FAILED로 기록
     */
    @Transactional
    public void handleAfterFetch(String webhookId,
                                 String webhookTimestamp,
                                 PortoneWebhookPayload payload,
                                 PortOnePaymentResponse result) {

        // 1. 중복 webhook 검사 (멱등 처리)
        if (webhookEventRepository.findByWebhookId(webhookId).isPresent()) {
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

        // webhook_event 저장 (RECEIVED 상태)
        WebhookEvent event;
        try {
            event = WebhookEvent.received(
                    webhookId,
                    payload.getData().getPaymentId(),
                    payload.getType(),
                    webhookTimestamp
            );
            webhookEventRepository.save(event);
        } catch (DataIntegrityViolationException e) {
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

        try {
            // 2. Payment / Order 조회
            String paymentId = result.getPaymentId();

            Payment payment = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new ServiceException(ErrorCode.PORTONE_PAYMENT_NOT_FOUND));

            Order order = payment.getOrder();

            // 3. 결제 금액 검증 (취소/환불은 제외)
            String rawStatus = result.getStatus();
            boolean isRefunded = "REFUNDED".equalsIgnoreCase(rawStatus);
            boolean isCanceledLike = rawStatus != null &&
                    ("CANCELED".equalsIgnoreCase(rawStatus) ||
                            "CANCELLED".equalsIgnoreCase(rawStatus) ||
                            isRefunded);

            // 취소/환불은 amount 검증 스킵
            if (!isCanceledLike) {
                BigDecimal portoneAmount = result.amount() != null ? result.amount().total() : null;
                BigDecimal orderAmount = order.getTotalPrice();

                if (portoneAmount == null || orderAmount == null || portoneAmount.compareTo(orderAmount) != 0) {

                    log.error("[PORTONE_WEBHOOK] amount mismatch paymentId={}, portoneAmount={}, orderAmount={}", paymentId, portoneAmount, orderAmount);
                    throw new ServiceException(ErrorCode.PORTONE_API_ERROR);
                }
            }

            // 상태 변환
            PaymentStatus targetStatus = maptoPaymentStatus(result.getStatus());
            PaymentStatus currentStatus = payment.getStatus();

            // 이미 환불 완료 상태면 늦게 온 이벤트 무시 (멱등)
            if (currentStatus == PaymentStatus.REFUNDED && targetStatus != PaymentStatus.REFUNDED) {

                log.info("[PORTONE_WEBHOOK] ignore late/out-of-order event. paymentId={}, current={}, target={}, rawStatus={}", paymentId, currentStatus, targetStatus, result.getStatus());
                event.markProcessed();
                webhookEventRepository.save(event);
                return;
            }

            // 상태 전이 검증
            if (!payment.getStatus().canTransitToTargetStatus(targetStatus)) {
                throw new ServiceException(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION);
            }

            // 4. 결제 상태 변경
            switch (targetStatus) {

                // 결제 승인 → 재고 차감, 주문 완료
                case APPROVED -> {
                    decreaseStockForOrder(order);
                    payment.paymentConfirmed();
                    order.orderCompleted();
                }

                // 결제 실패
                case FAILED -> payment.paymentFailed();

                // 결제 취소
                case CANCELED -> {
                    payment.paymentCanceled();
                    order.orderRefunded();
                }

                // 환불 완료
                case REFUNDED -> {
                    payment.paymentRefunded();
                    order.orderRefunded();
                }

                // 환불 실패
                case REFUND_FAILED -> payment.paymentRefundFailed();

                // 결제 대기 상태
                case PENDING -> { }
            }

            // 처리 완료 기록
            event.markProcessed();

        } catch (ServiceException ex) {

            // 처리 실패 기록
            log.error("[PORTONE_WEBHOOK] processing failed webhookId={}, code={}", webhookId, ex.getErrorCode().getCode(), ex);
            event.markFailed();

        } catch (Exception ex) {

            log.error("[PORTONE_WEBHOOK] unexpected error webhookId={}", webhookId, ex);
            event.markFailed();
        }
    }

    /**
     * PortOne 결제 상태(String)를 우리 시스템의 PaymentStatus(Enum)로 변환
     */
    private PaymentStatus maptoPaymentStatus(String status) {

        // status가 없는 경우 예외 처리
        if (status == null) { throw new ServiceException(ErrorCode.PORTONE_RESPONSE_NULL); }

        // PortOne 상태 값을 우리 시스템 상태로 변환
        return switch (status.toUpperCase()) {
            // 결제 완료
            case "PAID", "APPROVED", "COMPLETED" -> PaymentStatus.APPROVED;
            // 결제 실패
            case "FAILED" -> PaymentStatus.FAILED;
            // 결제 취소
            case "CANCELED", "CANCELLED" -> PaymentStatus.CANCELED;
            // 환불 완료
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            // 결제 대기 상태
            case "PENDING", "READY" -> PaymentStatus.PENDING;
            // 알 수 없는 상태 예외 처리
            default -> throw new ServiceException(ErrorCode.PORTONE_API_ERROR);
        };
    }

    /**
     * 주문에 포함된 상품들의 재고를 차감
     *
     * 흐름:
     * 1) 주문 상품 목록 조회
     * 2) 상품 ID별로 차감할 수량 계산
     * 3) 상품을 비관적 락으로 조회 (동시성 방어)
     * 4) 각 상품의 재고 감소
     */
    private void decreaseStockForOrder(Order order) {

        // 주문 상품 목록 조회
        List<OrderProduct> orderProducts = order.getOrderProducts();

        if (orderProducts == null || orderProducts.isEmpty()) return;

        // 상품별 차감 수량 계산
        Map<Long, Long> qtyByProductId = new HashMap<>();

        for (OrderProduct op : orderProducts) {

            if (op.getProduct() == null || op.getProduct().getId() == null) continue;

            Long productId = op.getProduct().getId();
            Long qty = op.getStock();

            if (qty == null || qty <= 0) continue;

            qtyByProductId.merge(productId, qty, Long::sum);
        }

        if (qtyByProductId.isEmpty()) return;

        List<Long> productIds = new ArrayList<>(qtyByProductId.keySet());

        // 상품을 비관적 락으로 조회(동시에 여러 결제가 재고를 수정하는 상황 방지)
        List<Product> products = productRepository.findAllByIdIn(productIds);

        // 조회된 상품 수가 다르면 예외 처리
        if (products.size() != productIds.size()) {
            throw new ServiceException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 상품 ID → Product 매핑
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 재고 차감
        for (Map.Entry<Long, Long> e : qtyByProductId.entrySet()) {
            Long productId = e.getKey();
            Long qty = e.getValue();

            Product product = productMap.get(productId);
            if (product == null) throw new ServiceException(ErrorCode.PRODUCT_NOT_FOUND);

            // 실제 재고 감소
            product.decreaseStock(qty);
        }
    }
}

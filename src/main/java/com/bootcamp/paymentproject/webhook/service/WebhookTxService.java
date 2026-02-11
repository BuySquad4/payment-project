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
     * PortOne webhook 처리 흐름
     * 1) webhookId 멱등 처리(중복이면 무시)
     //* 2) webhook_event 저장(RECEIVED)
     * 3) PortOne 결제 조회(SSOT)
     * 4) 결제/주문 상태 DB 반영
     * 5) webhook_event 처리 결과 기록(PROCESSED/FAILED)
     */
    @Transactional
    public void handleAfterFetch(String webhookId,
                                 String webhookTimestamp,
                                 PortoneWebhookPayload payload,
                                 PortOnePaymentResponse result) {

        if (webhookEventRepository.findByWebhookId(webhookId).isPresent()) {
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

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
            String paymentId = result.getPaymentId();

            Payment payment = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new ServiceException(ErrorCode.PORTONE_PAYMENT_NOT_FOUND));

            Order order = payment.getOrder();

            BigDecimal portoneAmount = result.amount() != null ? result.amount().total() : null;
            BigDecimal orderAmount = order.getTotalPrice();

            if (portoneAmount == null || orderAmount == null || portoneAmount.compareTo(orderAmount) != 0) {
                log.error("[PORTONE_WEBHOOK] amount mismatch paymentId={}, portoneAmount={}, orderAmount={}",
                        paymentId, portoneAmount, orderAmount);
                throw new ServiceException(ErrorCode.PORTONE_API_ERROR);
            }

            PaymentStatus targetStatus = maptoPaymentStatus(result.getStatus());

            if (!payment.getStatus().canTransitToTargetStatus(targetStatus)) {
                log.warn("[PORTONE_WEBHOOK] invalid transition paymentId={}, {} -> {}",
                        paymentId, payment.getStatus(), targetStatus);
                throw new ServiceException(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION);
            }

            switch (targetStatus) {
                case APPROVED -> {
                    decreaseStockForOrder(order);
                    payment.paymentConfirmed();
                    order.orderCompleted();
                    log.info("[PORTONE_WEBHOOK] payment approved paymentId={}", paymentId);
                }
                case FAILED -> payment.paymentFailed();
                case CANCELED -> {
                    payment.paymentCanceled();
                    order.orderRefunded();
                }
                case PENDING -> log.info("[PORTONE_WEBHOOK] payment pending paymentId={}", paymentId);
            }

            event.markProcessed();

        } catch (ServiceException ex) {
            log.error("[PORTONE_WEBHOOK] processing failed webhookId={}, code={}",
                    webhookId, ex.getErrorCode().getCode(), ex);
            event.markFailed();
        } catch (Exception ex) {
            log.error("[PORTONE_WEBHOOK] unexpected error webhookId={}", webhookId, ex);
            event.markFailed();
        }
    }

    /**
     * PortOne status(String) -> 우리 PaymentStatus(Enum) 변환
     */
    private PaymentStatus maptoPaymentStatus(String status) {

        if (status == null) { throw new ServiceException(ErrorCode.PORTONE_RESPONSE_NULL); }

        return switch (status.toUpperCase()) {
            case "PAID", "APPROVED", "COMPLETED" -> PaymentStatus.APPROVED;
            case "FAILED" -> PaymentStatus.FAILED;
            case "CANCELED", "CANCELLED", "REFUNDED" -> PaymentStatus.CANCELED;
            case "PENDING", "READY" -> PaymentStatus.PENDING;
            default -> throw new ServiceException(ErrorCode.PORTONE_API_ERROR);
        };
    }

    /**
     * 재고 차감 (Order.orderProducts 기준)
     * - ProductRepository.findAllByIdIn()에 PESSIMISTIC_WRITE가 걸려있어서 동시성 방어됨
     * - Product.decreaseStock(qty) 내부에서 "재고 부족"이면 예외 던지도록 하는 게 안전함(롤백)
     */
    private void decreaseStockForOrder(Order order) {
        List<OrderProduct> orderProducts = order.getOrderProducts();
        if (orderProducts == null || orderProducts.isEmpty()) return;

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

        // 비관락으로 상품 행 잠금
        List<Product> products = productRepository.findAllByIdIn(productIds);

        // 조회 누락 방어
        if (products.size() != productIds.size()) {
            // ErrorCode.PRODUCT_NOT_FOUND 없으면 PORTONE_API_ERROR로 임시 대체 가능
            throw new ServiceException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 재고 차감
        for (Map.Entry<Long, Long> e : qtyByProductId.entrySet()) {
            Long productId = e.getKey();
            Long qty = e.getValue();

            Product product = productMap.get(productId);
            if (product == null) throw new ServiceException(ErrorCode.PRODUCT_NOT_FOUND);

            product.decreaseStock(qty); // Product 안에 이미 있음
        }
    }
}

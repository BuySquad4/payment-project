package com.bootcamp.paymentproject.webhook.service;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import com.bootcamp.paymentproject.membership.repository.UserMembershipRepository;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.entity.OrderProduct;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
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
import java.time.LocalDateTime;
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

    private final PointTransactionRepository pointTransactionRepository;
    private final UserMembershipRepository userMembershipRepository;


    /**
     * PortOne webhook 결과를 DB에 반영하는 트랜잭션 처리
     * - 멱등 처리(중복 webhook 무시)
     * - 결제 상태 반영 + 재고 차감 + 포인트 적립/정리
     * - webhook_event 처리 결과(PROCESSED/FAILED) 기록
     */
    @Transactional
    public void handleAfterFetch(String webhookId,
                                 String webhookTimestamp,
                                 PortoneWebhookPayload payload,
                                 PortOnePaymentResponse result) {

        // 1) 멱등 처리: 같은 webhookId면 무시
        if (webhookEventRepository.findByWebhookId(webhookId).isPresent()) {
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

        // 1-1) webhook_event 저장 (중복이면 예외로도 방어)
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
            // 2) Payment / Order 조회
            String paymentId = result.getPaymentId();

            Payment payment = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new ServiceException(ErrorCode.PORTONE_PAYMENT_NOT_FOUND));

            Order order = payment.getOrder();

            // 3) 결제 금액 검증 (취소/환불은 제외)
            String rawStatus = result.getStatus();
            boolean isRefunded = "REFUNDED".equalsIgnoreCase(rawStatus);
            boolean isCanceledLike = rawStatus != null &&
                    ("CANCELED".equalsIgnoreCase(rawStatus) ||
                            "CANCELLED".equalsIgnoreCase(rawStatus) ||
                            isRefunded);

            if (!isCanceledLike) {
                BigDecimal portoneAmount = result.amount() != null ? result.amount().total() : null;
                BigDecimal orderAmount = order.getTotalPrice();

                if (portoneAmount == null || orderAmount == null || portoneAmount.compareTo(orderAmount) != 0) {

                    log.error("[PORTONE_WEBHOOK] amount mismatch paymentId={}, portoneAmount={}, orderAmount={}", paymentId, portoneAmount, orderAmount);
                    throw new ServiceException(ErrorCode.PORTONE_API_ERROR);
                }
            }

            // 4) 상태 매핑 + 상태 전이 검증
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

            // 5) 상태별 처리
            switch (targetStatus) {

                // 결제 승인: 재고 차감 + 주문 완료 + paidAt/refundableUntil 세팅 + HOLDING 적립
                case APPROVED -> {
                    decreaseStockForOrder(order);
                    payment.approve(LocalDateTime.now());
                    order.orderCompleted();

                    Long userId = order.getUser().getId();
                    Long orderId = order.getId();

                    // 중복 방지: 동일 주문에 HOLDING/EARN 있으면 생성 스킵
                    boolean hasHolding = pointTransactionRepository
                            .findFirstByUser_IdAndOrder_IdAndType(userId, orderId, PointType.HOLDING)
                            .isPresent();
                    boolean hasEarn = pointTransactionRepository
                            .findFirstByUser_IdAndOrder_IdAndType(userId, orderId, PointType.EARN)
                            .isPresent();

                    if (!hasHolding && !hasEarn) {
                        BigDecimal earnRate = userMembershipRepository.findEarnRateByUserId(userId)
                                .orElse(BigDecimal.ZERO);

                        BigDecimal earnAmount = payment.getAmount().multiply(earnRate);

                        pointTransactionRepository.save(new PointTransaction(earnAmount, PointType.HOLDING, order));
                    } else {
                        log.info("[POINT] skip holding creation. orderId={}, hasHolding={}, hasEarn={}",
                                orderId, hasHolding, hasEarn);
                    }
                }

                // 결제 실패
                case FAILED -> payment.paymentFailed();

                // 결제 취소 : 결제 취소 + 주문 환불 상태 반영
                case CANCELED -> {
                    payment.paymentCanceled();
                    order.orderRefunded();
                }

                // 환불: 결제 환불 + 주문 환불 + 해당 주문 적립(HOLDING/EARN) 잔액 0 마감
                case REFUNDED -> {
                    payment.refund(LocalDateTime.now());
                    order.orderRefunded();

                    Long userId = order.getUser().getId();
                    Long orderId = order.getId();

                    List<PointTransaction> earnLikeTxs =
                            pointTransactionRepository.findAllByUser_IdAndOrder_IdAndTypeIn(
                                    userId,
                                    orderId,
                                    List.of(PointType.HOLDING, PointType.EARN)
                            );

                    for (PointTransaction tx : earnLikeTxs) {
                        if (tx.getRemainingPoints() != null && tx.getRemainingPoints().compareTo(BigDecimal.ZERO) > 0) {
                            tx.updateRemainingPoints(BigDecimal.ZERO);
                        }
                    }

                    log.info("[POINT] refund zero-out earn-like txs. orderId={}, count={}", orderId, earnLikeTxs.size());
                }

                // 환불 실패
                case REFUND_FAILED -> payment.paymentRefundFailed();

                // 결제 대기 상태
                case PENDING -> { }
            }

            // 6) webhook_event 처리 완료
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

    /** PortOne status(String) -> PaymentStatus 변환 */
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

    /** 주문 상품 재고 차감(상품별 합산 후 비관적 락 조회로 동시성 방어) */
    private void decreaseStockForOrder(Order order) {

        List<OrderProduct> orderProducts = order.getOrderProducts();
        if (orderProducts == null || orderProducts.isEmpty()) return;

        // productId별 차감 수량 합산
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

        if (products.size() != productIds.size()) {
            throw new ServiceException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 재고 차감
        for (Map.Entry<Long, Long> e : qtyByProductId.entrySet()) {
            Product product = productMap.get(e.getKey());
            if (product == null) throw new ServiceException(ErrorCode.PRODUCT_NOT_FOUND);

            product.decreaseStock(e.getValue());
        }
    }
}

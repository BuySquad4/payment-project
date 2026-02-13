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
                    .orElse(null);

            if (payment == null) {
                log.warn("[PORTONE_WEBHOOK] payment not found yet. paymentId={}, webhookId={}", paymentId, webhookId);
                event.markProcessed();
                webhookEventRepository.save(event);
                return;
            }

            Order order = payment.getOrder();

            // 3) 결제 금액 검증 (취소/환불은 제외)
            PaymentStatus targetStatus = maptoPaymentStatus(result.getStatus());
            PaymentStatus currentStatus = payment.getStatus();

            if (targetStatus == PaymentStatus.PENDING) {
                log.info("[PORTONE_WEBHOOK] pending/ready event ignore. paymentId={}, current={}, rawStatus={}",
                        paymentId, currentStatus, result.getStatus());
                event.markProcessed();
                webhookEventRepository.save(event);
                return;
            }

            if (currentStatus == PaymentStatus.REFUNDED && targetStatus != PaymentStatus.REFUNDED) {
                log.info("[PORTONE_WEBHOOK] ignore late/out-of-order event. paymentId={}, current={}, target={}, rawStatus={}",
                        paymentId, currentStatus, targetStatus, result.getStatus());
                event.markProcessed();
                webhookEventRepository.save(event);
                return;
            }

            if (!currentStatus.canTransitToTargetStatus(targetStatus)) {
                log.info("[PORTONE_WEBHOOK] ignore non-forward transition. paymentId={}, current={}, target={}, rawStatus={}",
                        paymentId, currentStatus, targetStatus, result.getStatus());
                event.markProcessed();
                webhookEventRepository.save(event);
                return;
            }

            // 4) 결제 금액 검증은 "결제 완료(APPROVED)"에서만 수행
            if (targetStatus == PaymentStatus.APPROVED) {
                BigDecimal portoneAmount = result.amount() != null ? result.amount().total() : null;
                BigDecimal expectedAmount = payment.getAmount(); // 결제 생성 시 저장된 "실결제 예정 금액"

                if (portoneAmount == null || expectedAmount == null || portoneAmount.compareTo(expectedAmount) != 0) {
                    log.error("[PORTONE_WEBHOOK] amount mismatch paymentId={}, portoneAmount={}, expectedAmount={}",
                            paymentId, portoneAmount, expectedAmount);
                    throw new ServiceException(ErrorCode.PORTONE_API_ERROR);
                }
            }

            // 5) 상태별 처리
            switch (targetStatus) {

                // 결제 승인: 재고 차감 + 주문 완료 + paidAt/refundableUntil 세팅 + HOLDING 적립
                case APPROVED -> {
                    decreaseStockForOrder(order);
                    payment.approve(LocalDateTime.now());
                    order.orderCompleted();

                    // 포인트는 "부가 처리": 실패해도 webhook 전체를 FAILED로 만들지 않게 분리
                    try {
                        Long userId = order.getUser().getId();
                        Long orderId = order.getId();

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

                            pointTransactionRepository.save(
                                    new PointTransaction(earnAmount, PointType.HOLDING, order)
                            );
                        } else {
                            log.info("[POINT] skip holding creation. orderId={}, hasHolding={}, hasEarn={}",
                                    orderId, hasHolding, hasEarn);
                        }
                    } catch (Exception e) {
                        // 포인트만 실패 처리(결제/주문 반영은 성공으로 간주)
                        log.error("[POINT] approved point handling failed. paymentId={}, orderId={}",
                                paymentId, order.getId(), e);
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

                    // 1) 적립(HOLDING/EARN) 회수: remaining 0 마감
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

                    // 2) 사용 포인트 복구
                    restoreSpentPointsOnRefund(order);
                }

                // 환불 실패
                case REFUND_FAILED -> payment.paymentRefundFailed();

                // 결제 대기 상태
                case PENDING -> { }
            }

            // 6) webhook_event 처리 완료
            event.markProcessed();
            webhookEventRepository.save(event);

        } catch (ServiceException ex) {

            // 처리 실패 기록
            log.error("[PORTONE_WEBHOOK] processing failed webhookId={}, code={}", webhookId, ex.getErrorCode().getCode(), ex);
            event.markFailed();
            webhookEventRepository.save(event);

        } catch (Exception ex) {
            log.error("[PORTONE_WEBHOOK] unexpected error webhookId={}", webhookId, ex);
            event.markFailed();
            webhookEventRepository.save(event);
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

    private void restoreSpentPointsOnRefund(Order order) {
        Long userId = order.getUser().getId();
        Long orderId = order.getId();

        BigDecimal used = order.getPointToUse(); // orders.point_to_use
        if (used == null || used.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 멱등 처리 : 이미 CANCEL(복구) 트랜잭션이 있으면 중복 생성 방지
        boolean hasCancel = pointTransactionRepository
                .findFirstByUser_IdAndOrder_IdAndType(userId, orderId, PointType.CANCEL)
                .isPresent();

        if (hasCancel) {
            log.info("[POINT] already restored(spent) by CANCEL. orderId={}, amount={}", orderId, used);
            return;
        }

        // CANCEL 트랜잭션 생성 (+used)
        PointTransaction cancelTx = new PointTransaction(used, PointType.CANCEL, order);

        // 중요 : 잔고가 remaining_points 기반이면 반드시 복구분만큼 remaining_points를 살려야 함
        cancelTx.updateRemainingPoints(used);

        pointTransactionRepository.save(cancelTx);

        log.info("[POINT] restored spent points on refund. orderId={}, amount={}", orderId, used);
    }
}

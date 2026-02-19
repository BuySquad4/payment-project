package com.bootcamp.paymentproject.refund.service;

import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.payment.dto.response.RefundPaymentResponse;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.exception.PaymentNotFoundException;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.point.entity.PointSpendHistory;
import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointSpendHistoryRepository;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.refund.entity.Refund;
import com.bootcamp.paymentproject.refund.enums.RefundState;
import com.bootcamp.paymentproject.refund.exception.PaymentNotRefundableException;
import com.bootcamp.paymentproject.refund.exception.RefundNotFoundException;
import com.bootcamp.paymentproject.refund.repository.RefundRepository;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.exception.UserNotFoundException;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;
    private final PointSpendHistoryRepository pointSpendHistoryRepository;

    @Transactional
    public boolean checkPaymentRefundable(String paymentId, String reason, PortOnePaymentResponse portOnePaymentResponse) {

        if(portOnePaymentResponse == null || !portOnePaymentResponse.isPaid() || portOnePaymentResponse.isCancelled()){
            throw new PaymentNotRefundableException();
        }

        Payment dbPayment = paymentRepository.findByPaymentId(paymentId).orElseThrow(
                PaymentNotFoundException::new
        );

        // 환불 가능 기간 체크
        if (!dbPayment.isRefundable(LocalDateTime.now())) {
            throw new PaymentNotRefundableException();
        }

        Refund refund = new Refund(
                dbPayment.getAmount(),
                reason,
                RefundState.REQUESTED,
                null,
                dbPayment
        );

        // 멱등성 검사 (unique 키 제약조건 사용)
        try{
            refundRepository.saveAndFlush(refund);
        } catch (DataIntegrityViolationException e){
            Refund existRefund = refundRepository.findByPaymentId(paymentId).orElseThrow(
                    RefundNotFoundException::new
            );

            switch (existRefund.getState()){
                case REQUESTED, COMPLETED -> {return false;}
                case FAILED -> existRefund.updateToTargetState(RefundState.REQUESTED);
            }
        }

        dbPayment.paymentCanceled();
        dbPayment.getOrder().orderPendingRefund();

        return true;
    }

    /**
     * PortOne 최종 상태에 따라 환불 완료/실패를 DB에 반영 + 포인트 처리
     */
    @Transactional
    public RefundPaymentResponse refundPaymentTransaction(String paymentId ,PortOnePaymentResponse portOnePaymentResponse) {

        Payment dbPayment = paymentRepository.findByPaymentId(paymentId).orElseThrow(
                PaymentNotFoundException::new);

        Refund dbRefund = refundRepository.findByPaymentId(paymentId).orElseThrow(
                RefundNotFoundException::new);

        if (portOnePaymentResponse != null && portOnePaymentResponse.isCancelled()) {

            LocalDateTime now = LocalDateTime.now();

            // 결제/주문/환불 상태 변경 + 시각 저장
            dbPayment.refund(now);
            dbPayment.getOrder().orderRefunded();

            dbRefund.updateRefundedAt(now);
            dbRefund.updateToTargetState(RefundState.COMPLETED);

            // 포인트 복구/회수 처리
            handlePointRollback(dbPayment);

        } else {
            dbPayment.paymentRefundFailed();
            dbPayment.getOrder().orderRefundFailed();
            dbRefund.updateToTargetState(RefundState.FAILED);
        }

        return RefundPaymentResponse.fromEntity(dbPayment);
    }

    private void handlePointRollback(Payment dbPayment) {

        // 주문/사용자 식별
        Order order = dbPayment.getOrder();
        Long orderId = order.getId();
        Long userId = order.getUser().getId();

        // 최신 User 엔티티로 영속성 보장(변경사항 저장 목적)
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 0) 멱등 처리: 이미 CANCEL 이력이 있으면 환불/복구 중복 실행 방지
        boolean alreadyCanceled = pointTransactionRepository
                .findFirstByUser_IdAndOrder_IdAndType(userId, orderId, PointType.CANCEL)
                .isPresent();

        if (alreadyCanceled) {
            return;
        }

        // 1) SPENT 복구: 사용한 포인트를 EARN.remainingPoints로 다시 채워 넣음
        List<PointTransaction> spentTxs =
                pointTransactionRepository.findAllByUser_IdAndOrder_IdAndType(
                        userId, orderId, PointType.SPENT
                );

        BigDecimal totalRestore = BigDecimal.ZERO; // 최종 복구 금액
        boolean restoredByHistory = false;

        // spendHistory 기반 복구
        for (PointTransaction spentTx : spentTxs) {
            List<PointSpendHistory> histories = pointSpendHistoryRepository.findAllBySpendTransaction(spentTx);

            if (histories == null || histories.isEmpty()) {
                continue;
            }

            restoredByHistory = true;

            for (PointSpendHistory h : histories) {
                PointTransaction earnTx = h.getEarnTransaction();
                BigDecimal used = h.getAmount();

                if (earnTx == null || used == null || used.compareTo(BigDecimal.ZERO) <= 0) continue;

                // remaining += used
                BigDecimal currentRem = earnTx.getRemainingPoints() == null ? BigDecimal.ZERO : earnTx.getRemainingPoints();
                BigDecimal max = earnTx.getPoints() == null ? BigDecimal.ZERO : earnTx.getPoints();

                BigDecimal restored = currentRem.add(used);
                if (restored.compareTo(max) > 0) {
                    restored = max;
                }

                earnTx.updateRemainingPoints(restored);
                totalRestore = totalRestore.add(used); // 복구 금액 누적
            }
        }

        // history 없으면: SPENT 총액만큼 EARN remaining을 순서대로 채움
        if (!restoredByHistory) {
            BigDecimal spentTotal = BigDecimal.ZERO;
            for (PointTransaction spentTx : spentTxs) {
                if (spentTx.getPoints() != null) {
                    spentTotal = spentTotal.add(spentTx.getPoints().abs());
                }
            }

            if (spentTotal.compareTo(BigDecimal.ZERO) > 0) {
                List<PointTransaction> earnTxs = pointTransactionRepository.findEarnTransactionsByUserID(userId, PointType.EARN);

                BigDecimal toRestore = spentTotal;

                for (PointTransaction earnTx : earnTxs) {
                    if (toRestore.compareTo(BigDecimal.ZERO) <= 0) break;

                    BigDecimal currentRem = earnTx.getRemainingPoints() == null ? BigDecimal.ZERO : earnTx.getRemainingPoints();
                    BigDecimal max = earnTx.getPoints() == null ? BigDecimal.ZERO : earnTx.getPoints();

                    BigDecimal capacity = max.subtract(currentRem);
                    if (capacity.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal add = toRestore.min(capacity);

                    earnTx.updateRemainingPoints(currentRem.add(add));
                    totalRestore = totalRestore.add(add);
                    toRestore = toRestore.subtract(add);
                }
            }
        }

        // 복구 금액은 양수로 통일
        if (totalRestore == null) totalRestore = BigDecimal.ZERO;
        totalRestore = totalRestore.abs();

        // 2) HOLDING 처리: remainingPoints는 건드리지 않고 "무효화 기록"만 남김(CANCEL -금액)
        List<PointTransaction> holdingTxs =
                pointTransactionRepository.findAllByUser_IdAndOrder_IdAndType(
                        userId, orderId, PointType.HOLDING
                );

        BigDecimal holdingTotal = BigDecimal.ZERO;
        for (PointTransaction tx : holdingTxs) {
            if (tx.getPoints() != null) {
                holdingTotal = holdingTotal.add(tx.getPoints().abs());
            }
        }

        // 3) CANCEL 기록 생성
        //     - SPENT 복구: CANCEL +금액
        //     - HOLDING 무효화: CANCEL -금액
        if (totalRestore != null && totalRestore.compareTo(BigDecimal.ZERO) > 0) {
            pointTransactionRepository.save(PointTransaction.cancelSpendRestore(user, order, totalRestore));
        }
        if (holdingTotal != null && holdingTotal.compareTo(BigDecimal.ZERO) > 0) {
            pointTransactionRepository.save(PointTransaction.cancelHolding(user, order, holdingTotal));
        }

        // 4) 최종 동기화: user.pointBalance = 모든 EARN.remainingPoints 합
        syncUserPointBalance(user);
    }

    /**
     * 사용자 잔액을 "EARN.remainingPoints 합"으로 다시 계산해서 저장
     */
    private void syncUserPointBalance(User user) {
        BigDecimal remainingPoint =
                pointTransactionRepository.getPointSumByUserId(user.getId(), PointType.EARN);

        if (remainingPoint == null) remainingPoint = BigDecimal.ZERO;

        user.setPointBalance(remainingPoint);
        userRepository.save(user);
    }
}

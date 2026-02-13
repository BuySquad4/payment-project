package com.bootcamp.paymentproject.refund.service;

import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.payment.dto.response.RefundPaymentResponse;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.exception.PaymentNotFoundException;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.portone.client.PortOneClient;
import com.bootcamp.paymentproject.refund.entity.Refund;
import com.bootcamp.paymentproject.refund.enums.RefundState;
import com.bootcamp.paymentproject.refund.exception.PaymentNotRefundableException;
import com.bootcamp.paymentproject.refund.exception.RefundNotFoundException;
import com.bootcamp.paymentproject.refund.repository.RefundRepository;
import com.bootcamp.paymentproject.user.entity.User;
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
    private final PortOneClient portOneClient;
    private final PointTransactionRepository pointTransactionRepository;

    /**
     * 환불 "완료 처리" 흐름
     * 1) 주문(orderId)로 결제 조회
     * 2) 본인 주문/환불 가능 기간 체크
     * 3) PortOne 결제 상태 조회
     * 4) 환불 요청 생성(멱등) + DB 상태를 "환불 대기"로 변경
     * 5) 다시 PortOne 조회 후 환불 완료면 DB 반영 + 포인트 처리
     */
    @Transactional
    public RefundPaymentResponse refund(Long userId, Long orderId, String reason) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentNotFoundException::new);

        // 본인 주문인지 확인
        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new PaymentNotRefundableException();
        }

        // 환불 가능 기간 체크
        if (!payment.isRefundable(LocalDateTime.now())) {
            throw new PaymentNotRefundableException();
        }

        // PortOne 결제 상태 조회
        PortOnePaymentResponse portOnePaymentResponse = portOneClient.getPayment(payment.getPaymentId());

        // 환불 요청 생성(멱등)
        boolean requested = checkPaymentRefundable(
                payment.getPaymentId(),
                "사용자 환불 요청",
                portOnePaymentResponse
        );

        // 이미 REQUESTED/COMPLETED면 추가 처리 없이 반환
        if (!requested) {
            return RefundPaymentResponse.fromEntity(payment);
        }

        // 다시 PortOne 상태 조회 (환불 완료 여부 확인)
        PortOnePaymentResponse latest = portOneClient.getPayment(payment.getPaymentId());

        // 환불 결과 반영 + 포인트 처리
        return refundPaymentTransaction(payment.getPaymentId(), latest);
    }

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
                PaymentNotFoundException::new
        );

        Refund dbRefund = refundRepository.findByPaymentId(paymentId).orElseThrow(
                RefundNotFoundException::new
        );

        if(portOnePaymentResponse.isCancelled()){

            LocalDateTime now = LocalDateTime.now();

            // 결제/주문/환불 상태 변경 + 시각 저장
            dbPayment.refund(now);
            dbPayment.getOrder().orderRefunded();

            dbRefund.updateRefundedAt(now);
            dbRefund.updateToTargetState(RefundState.COMPLETED);

            // ===================
            // 포인트 복구 / 취소 처리
            // ===================

            // 환불 대상 사용자/주문 조회
            Long userId = dbPayment.getOrder().getUser().getId();
            Long orderId = dbPayment.getOrder().getId();

            User user = dbPayment.getOrder().getUser();
            Order order = dbPayment.getOrder();

            // 1) 사용 포인트 복구 : SPENT -> CANCEL(양수)
            // SPENT 트랜잭션이 존재하면 동일 금액을 CANCEL(양수)로 생성하여 포인트 복구
            pointTransactionRepository
                    .findFirstByUser_IdAndOrder_IdAndType(userId, orderId, PointType.SPENT)
                    .ifPresent(spentTx -> {
                        BigDecimal restore = spentTx.getPoints().abs(); // 복구할 포인트 (양수)
                        pointTransactionRepository.save(
                                PointTransaction.cancel(user, order, restore)
                        );
                    });

            // 2) 적립 포인트 회수
            // 해당 주문으로 적립된 HOLDING/EARN 트랜잭션의 remainingPoints를 0으로 마감
            // 동시에 회수된 총 포인트를 CANCEL 트랜잭션으로 기록
            List<PointTransaction> earnLikeTxs =
                    pointTransactionRepository.findAllByUser_IdAndOrder_IdAndTypeIn(
                            userId, orderId, List.of(PointType.HOLDING, PointType.EARN)
                    );

            BigDecimal totalReclaimed = BigDecimal.ZERO;

            for (PointTransaction tx : earnLikeTxs) {
                BigDecimal rem = tx.getRemainingPoints();
                if (rem != null && rem.compareTo(BigDecimal.ZERO) > 0) {
                    totalReclaimed = totalReclaimed.add(rem);     // 회수 포인트 누적
                    tx.updateRemainingPoints(BigDecimal.ZERO);    // 잔액 마감
                }
            }

            // 회수된 포인트가 존재하면 CANCEL 트랜잭션 생성 (환불로 인한 적립 취소 기록)
            if (totalReclaimed.compareTo(BigDecimal.ZERO) > 0) {
                pointTransactionRepository.save(
                        PointTransaction.cancel(user, order, totalReclaimed)
                );
            }

            // ===================
            // 포인트 복구 / 취소 처리 끝
            // ===================

        } else {
            dbPayment.paymentRefundFailed();
            dbPayment.getOrder().orderRefundFailed();

            dbRefund.updateToTargetState(RefundState.FAILED);
        }

        return RefundPaymentResponse.fromEntity(dbPayment);
    }
}

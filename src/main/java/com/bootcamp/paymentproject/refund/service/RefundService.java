package com.bootcamp.paymentproject.refund.service;

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
     * 컨트롤러에서 호출: 환불 "완료 처리"
     * - orderId로 결제 찾기
     * - 환불가능기간 체크
     * - PortOne 결제 상태 조회
     * - 환불 요청 생성(멱등)
     * - 다시 조회 후 환불 완료 반영 + 포인트 복구/취소
     */
    @Transactional
    public RefundPaymentResponse refund(Long userId, Long orderId, String reason) {

        // 1) orderId로 Payment 찾기
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentNotFoundException::new);

        // 2) 본인 주문인지 확인
        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new PaymentNotRefundableException();
        }

        // 3) 환불 가능 기간 체크
        if (!payment.isRefundable(LocalDateTime.now())) {
            throw new PaymentNotRefundableException();
        }

        // 4) PortOne 결제 상태 조회
        PortOnePaymentResponse portOnePaymentResponse = portOneClient.getPayment(payment.getPaymentId());

        // 5) 환불 요청 생성(멱등) + 상태 변경
        boolean requested = checkPaymentRefundable(
                payment.getPaymentId(),
                "사용자 환불 요청",
                portOnePaymentResponse
        );

        if (!requested) {
            // 이미 REQUESTED/COMPLETED 인 경우 여기서 종료
            return RefundPaymentResponse.fromEntity(payment);
        }

        // 6) 다시 PortOne 상태 조회 (환불 완료 여부 확인)
        PortOnePaymentResponse latest = portOneClient.getPayment(payment.getPaymentId());

        // 7) 환불 결과 반영 + 포인트 처리
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

            Long userId = dbPayment.getOrder().getUser().getId();
            Long orderId = dbPayment.getOrder().getId();

            // 1. 사용 포인트 복구 (SPENT -> CANCEL)
            pointTransactionRepository
                    .findFirstByUser_IdAndOrder_IdAndType(userId, orderId, PointType.SPENT)
                    .ifPresent(spentTx -> {

                        BigDecimal restoreAmount = spentTx.getPoints().abs();
                        PointTransaction cancelRestore =
                                PointTransaction.cancel(
                                        dbPayment.getOrder().getUser(),
                                        dbPayment.getOrder(),
                                        restoreAmount
                                );
                        pointTransactionRepository.save(cancelRestore);
                    });

            // 2. 적립 포인트 취소 (EARN/HOLDING -> CANCEL)
            List<PointTransaction> earnList =
                    pointTransactionRepository.findAllByUser_IdAndOrder_IdAndTypeIn(
                            userId,
                            orderId,
                            List.of(PointType.EARN, PointType.HOLDING)
                    );

            for(PointTransaction earnTx : earnList) {
                BigDecimal cancelAmount = earnTx.getPoints().abs().negate();
                PointTransaction cancelEarn =
                        PointTransaction.cancel(
                                dbPayment.getOrder().getUser(),
                                dbPayment.getOrder(),
                                cancelAmount
                        );
                pointTransactionRepository.save(cancelEarn);
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

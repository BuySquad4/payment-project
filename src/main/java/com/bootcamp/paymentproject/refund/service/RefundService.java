package com.bootcamp.paymentproject.refund.service;

import com.bootcamp.paymentproject.payment.dto.response.RefundPaymentResponse;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import com.bootcamp.paymentproject.payment.exception.PaymentNotFoundException;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.portone.PortOneClient;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.refund.entity.Refund;
import com.bootcamp.paymentproject.refund.enums.RefundState;
import com.bootcamp.paymentproject.refund.exception.PaymentNotRefundableException;
import com.bootcamp.paymentproject.refund.exception.RefundNotFoundException;
import com.bootcamp.paymentproject.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final RefundRepository refundRepository;
    private final PortOneClient portOneClient;
    private final PaymentRepository paymentRepository;

    @Transactional
    public boolean checkPaymentRefundable(String paymentId, String reason, PortOnePaymentResponse portOnePaymentResponse) {

        if(portOnePaymentResponse == null || !portOnePaymentResponse.isPaid() || portOnePaymentResponse.isCancelled()){
            throw new PaymentNotRefundableException();
        }

        Payment dbPayment = paymentRepository.findByPaymentId(paymentId).orElseThrow(
                PaymentNotFoundException::new
        );

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
            dbPayment.paymentRefunded();
            dbPayment.getOrder().orderRefunded();

            dbRefund.updateRefundedAt(LocalDateTime.now());
            dbRefund.updateToTargetState(RefundState.COMPLETED);
        } else {
            dbPayment.paymentRefundFailed();
            dbPayment.getOrder().orderRefundFailed();

            dbRefund.updateToTargetState(RefundState.FAILED);
        }

        return RefundPaymentResponse.fromEntity(dbPayment);
    }

}

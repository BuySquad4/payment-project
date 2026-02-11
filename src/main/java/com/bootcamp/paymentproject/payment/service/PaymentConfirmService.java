package com.bootcamp.paymentproject.payment.service;

import com.bootcamp.paymentproject.payment.dto.response.ConfirmPaymentResponse;
import com.bootcamp.paymentproject.portone.PortOneClient;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.refund.service.PaymentRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmService {
    private final PortOneClient portOneClient;
    private final PaymentService paymentService;
    private final PaymentRefundService paymentRefundService;

    // 외부 api 호출을 transaction 에서 분리
    public ConfirmPaymentResponse confirmPayment(String paymentId) {

        PortOnePaymentResponse payment = portOneClient.getPayment(paymentId);

        ConfirmPaymentResponse confirmPaymentResponse = paymentService.confirmPaymentTransaction(paymentId, payment);

        // 결제금액 상이/재고 문제 발생시, 환불 프로세스 진행
        if(confirmPaymentResponse.isRefundRequired()){
            paymentRefundService.refundPayment(paymentId, confirmPaymentResponse.getMessage(), payment);
        }

        return confirmPaymentResponse;
    }
}

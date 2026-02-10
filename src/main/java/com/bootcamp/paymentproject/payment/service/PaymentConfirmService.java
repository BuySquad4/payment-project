package com.bootcamp.paymentproject.payment.service;

import com.bootcamp.paymentproject.payment.dto.response.ConfirmPaymentResponse;
import com.bootcamp.paymentproject.portone.PortOneClient;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService {
    private final PortOneClient portOneClient;
    private final PaymentService paymentService;

    // 외부 api 호출을 transaction 에서 분리
    public ConfirmPaymentResponse confirmPayment(String paymentId) {

        PortOnePaymentResponse payment = portOneClient.getPayment(paymentId);
        ConfirmPaymentResponse confirmPaymentResponse = paymentService.confirmPaymentTransaction(paymentId, payment);

        // 결제금액 상이/재고 문제 발생시
        // 환불 프로세스 진행
        if(confirmPaymentResponse.isRefundRequired()){
            // RefundService 클래스 생성해서 메서드 호출
        }

        return confirmPaymentResponse;
    }
}

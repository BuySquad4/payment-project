package com.bootcamp.paymentproject.refund.service;

import com.bootcamp.paymentproject.common.properties.PortOneProperties;
import com.bootcamp.paymentproject.payment.dto.response.RefundPaymentResponse;
import com.bootcamp.paymentproject.portone.PortOneCancelRequest;
import com.bootcamp.paymentproject.portone.PortOneClient;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {
    private final RefundService refundService;
    private final PortOneClient portOneClient;
    private final PortOneProperties portOneProperties;

    public RefundPaymentResponse refundPayment(String paymentId, String reason, PortOnePaymentResponse portOnePaymentResponse) {

        // 결제 정보가 없을 경우 404 에러를 GlobalExceptionHandler에서 처리
        if(portOnePaymentResponse == null){
            portOnePaymentResponse = portOneClient.getPayment(paymentId);
        }

        // 환불 요청 멱등성 처리 + 환불 불가능 케이스 처리
        if (!refundService.checkPaymentRefundable(paymentId, reason, portOnePaymentResponse)) {
            return null;
        }

        // 포트원 전액 환불 요청
        portOneClient.cancelPayment(paymentId, PortOneCancelRequest.fullCancel(portOneProperties.getStore().getId(), reason));

        PortOnePaymentResponse payment = portOneClient.getPayment(paymentId);

        // 환불 처리가 제대로 진행되었는지 확인
        // DB에 데이터 상태 반영
        return refundService.refundPaymentTransaction(paymentId, payment);
    }
}

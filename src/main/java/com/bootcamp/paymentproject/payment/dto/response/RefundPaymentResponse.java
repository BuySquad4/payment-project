package com.bootcamp.paymentproject.payment.dto.response;

import com.bootcamp.paymentproject.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefundPaymentResponse {
    private final Long id;
    private final String paymentId;
    private final Long orderId;
    private final String status;

    public static RefundPaymentResponse fromEntity(Payment payment) {
        return new RefundPaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getOrder().getId(),
                payment.getStatus().toString()
        );
    }
}

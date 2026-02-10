package com.bootcamp.paymentproject.payment.dto.response;

import com.bootcamp.paymentproject.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfirmPaymentResponse {
    private final Long id;
    private final String paymentId;
    private final Long orderId;
    private final String status;
    private final String message;
    private final boolean refundRequired;

    public static ConfirmPaymentResponse fromEntityWithMessage(Payment payment, boolean refundRequired, String message) {
        return new ConfirmPaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getOrder().getId(),
                payment.getStatus().toString(),
                message,
                refundRequired
        );
    }
}

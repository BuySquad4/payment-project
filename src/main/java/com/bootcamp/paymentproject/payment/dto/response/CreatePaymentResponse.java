package com.bootcamp.paymentproject.payment.dto.response;

import com.bootcamp.paymentproject.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreatePaymentResponse {

    private final Long id;
    private final String paymentId;
    private final BigDecimal amount;
    private final String status;
    private final Long orderId;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static CreatePaymentResponse fromEntity(Payment payment) {
        return new CreatePaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getStatus().toString(),
                payment.getOrder().getId(),
                payment.getCreatedAt(),
                payment.getModifiedAt()
        );
    }
}

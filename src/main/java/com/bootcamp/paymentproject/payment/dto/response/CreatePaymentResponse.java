package com.bootcamp.paymentproject.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreatePaymentResponse {

    private final boolean success;
    private final String paymentId;
    private final String status;
}

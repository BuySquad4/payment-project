package com.bootcamp.paymentproject.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CreatePaymentRequest {
    private String orderId;
    private BigDecimal totalAmount;
    private BigDecimal pointsToUse;
}

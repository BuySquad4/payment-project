package com.bootcamp.paymentproject.webhook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PortonePaymentResponse {

    private String paymentId;
    private String status;
    private Amount amount;

    @Getter
    @NoArgsConstructor
    public static class Amount {
        private BigDecimal total;
    }
}

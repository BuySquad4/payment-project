package com.bootcamp.paymentproject.webhook.client;

import com.bootcamp.paymentproject.webhook.dto.PortonePaymentResponse;

public interface PortOneClient {
    PortonePaymentResponse getPayment(String paymentId);
}

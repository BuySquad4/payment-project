package com.bootcamp.paymentproject.portone.client;

import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;

public interface PortOneClient {
    PortOnePaymentResponse getPayment(String paymentId);
}

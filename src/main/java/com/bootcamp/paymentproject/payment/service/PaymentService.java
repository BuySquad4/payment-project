package com.bootcamp.paymentproject.payment.service;

import com.bootcamp.paymentproject.payment.dto.request.CreatePaymentRequest;
import com.bootcamp.paymentproject.payment.dto.response.CreatePaymentResponse;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        return null;
    }
}

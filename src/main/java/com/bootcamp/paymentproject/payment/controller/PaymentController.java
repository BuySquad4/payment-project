package com.bootcamp.paymentproject.payment.controller;

import com.bootcamp.paymentproject.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
}

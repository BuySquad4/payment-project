package com.bootcamp.paymentproject.payment.controller;

import com.bootcamp.paymentproject.payment.dto.request.CreatePaymentRequest;
import com.bootcamp.paymentproject.payment.dto.response.CreatePaymentResponse;
import com.bootcamp.paymentproject.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(@RequestBody CreatePaymentRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }
}

package com.bootcamp.paymentproject.payment.controller;

import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.payment.dto.request.CreatePaymentRequest;
import com.bootcamp.paymentproject.payment.dto.response.ConfirmPaymentResponse;
import com.bootcamp.paymentproject.payment.dto.response.CreatePaymentResponse;
import com.bootcamp.paymentproject.payment.service.PaymentConfirmService;
import com.bootcamp.paymentproject.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentConfirmService paymentConfirmService;

    @PostMapping
    public ResponseEntity<SuccessResponse<CreatePaymentResponse>> createPayment(@RequestBody CreatePaymentRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success(paymentService.createPayment(request), "결제 생성에 성공했습니다."));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<SuccessResponse<ConfirmPaymentResponse>> confirmPayment(@PathVariable String paymentId){
        ConfirmPaymentResponse response = paymentConfirmService.confirmPayment(paymentId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(response, response.getMessage()));
    }
}

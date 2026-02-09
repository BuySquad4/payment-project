package com.bootcamp.paymentproject.payment.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException() {
        super(ErrorCode.PAYMENT_NOT_FOUND);
    }
}

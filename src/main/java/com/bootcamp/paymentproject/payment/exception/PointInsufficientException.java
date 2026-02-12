package com.bootcamp.paymentproject.payment.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;

public class PointInsufficientException extends PaymentException {
    public PointInsufficientException() {
        super(ErrorCode.POINT_INSUFFICIENT);
    }
}

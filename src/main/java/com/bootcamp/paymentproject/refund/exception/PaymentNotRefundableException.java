package com.bootcamp.paymentproject.refund.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;

public class PaymentNotRefundableException extends RefundException {
    public PaymentNotRefundableException() {
        super(ErrorCode.PAYMENT_NOT_REFUNDABLE);
    }
}

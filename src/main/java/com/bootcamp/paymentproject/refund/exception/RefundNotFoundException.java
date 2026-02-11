package com.bootcamp.paymentproject.refund.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;

public class RefundNotFoundException extends RefundException {
    public RefundNotFoundException() {
        super(ErrorCode.REFUND_NOT_FOUND);
    }
}

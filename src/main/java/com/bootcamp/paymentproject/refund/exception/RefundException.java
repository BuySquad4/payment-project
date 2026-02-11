package com.bootcamp.paymentproject.refund.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;

public class RefundException extends ServiceException {
    public RefundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

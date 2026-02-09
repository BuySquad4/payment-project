package com.bootcamp.paymentproject.payment.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;

public class PaymentException extends ServiceException {
    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
}

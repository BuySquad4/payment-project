package com.bootcamp.paymentproject.order.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;

public class OrderException extends ServiceException {
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}

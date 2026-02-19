package com.bootcamp.paymentproject.order.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;

public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
}

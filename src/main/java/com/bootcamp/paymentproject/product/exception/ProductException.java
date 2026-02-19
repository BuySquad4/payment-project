package com.bootcamp.paymentproject.product.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;

public class ProductException extends ServiceException {

    public ProductException(ErrorCode errorCode) {
        super(
                errorCode
        );
    }
}
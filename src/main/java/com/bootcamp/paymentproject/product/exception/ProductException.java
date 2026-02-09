package com.bootcamp.paymentproject.product.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;

public class ProductException extends ServiceException {

    // 형우님의 ServiceException이 요구하는 생성자 형식에 맞춤
    public ProductException(ProductErrorCode errorCode) {
        super(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus(),
                ErrorCode.PAYMENT_NOT_FOUND // 임시로 형우님의 ErrorCode 하나를 같이 넘겨줌
        );
    }
}
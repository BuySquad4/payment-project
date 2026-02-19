package com.bootcamp.paymentproject.user.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import lombok.Getter;

@Getter
public class UserException extends ServiceException {

    public UserException(ErrorCode errorCode) {
        super(
                errorCode
        );

    }
}

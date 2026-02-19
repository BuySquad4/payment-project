package com.bootcamp.paymentproject.user.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super(ErrorCode.NOT_FOUND_USER);
    }
}

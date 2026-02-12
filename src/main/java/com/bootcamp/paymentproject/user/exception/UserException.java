package com.bootcamp.paymentproject.user.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserException extends RuntimeException {
    private final UserErrorCode errorCode;
    private final HttpStatus errorStatus;
    private final String errorMessage;

    public UserException(UserErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorStatus = errorCode.getStatus();
        this.errorMessage = errorCode.getMessage();
    }
}

package com.bootcamp.paymentproject.membership.exception;

import com.bootcamp.paymentproject.user.exception.UserErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MembershipException extends RuntimeException {
    private final MembershipErrorCode errorCode;
    private final HttpStatus errorStatus;
    private final String errorMessage;

    public MembershipException(MembershipErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorStatus = errorCode.getStatus();
        this.errorMessage = errorCode.getMessage();
    }
}
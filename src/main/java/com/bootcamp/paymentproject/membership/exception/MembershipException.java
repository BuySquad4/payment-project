package com.bootcamp.paymentproject.membership.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MembershipException extends ServiceException {
    public MembershipException(ErrorCode errorCode) {
        super(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getHttpStatus(),
                errorCode
        );
    }
}
package com.bootcamp.paymentproject.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "존재하지 않는 결제 정보 입니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

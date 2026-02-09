package com.bootcamp.paymentproject.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String code;
    private String message;
    private String uri;
    private LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode, String uri) {
        return new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                uri,
                LocalDateTime.now()
        );
    }
}

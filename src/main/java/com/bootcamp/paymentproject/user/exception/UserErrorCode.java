package com.bootcamp.paymentproject.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode {
    Duplicate_Email("P001", "중복된 이메일입니다", HttpStatus.CONFLICT),
    NOT_FOUND_USER("P002", "존재하지 않는 유저입니다", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

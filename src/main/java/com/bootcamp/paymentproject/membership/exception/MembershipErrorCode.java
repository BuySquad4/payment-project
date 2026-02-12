package com.bootcamp.paymentproject.membership.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MembershipErrorCode {
    NOT_FOUND_GRADE("P001", "존재하지 않는 등급입니다", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

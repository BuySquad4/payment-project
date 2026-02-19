package com.bootcamp.paymentproject.common.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ────────────────────────────────────────────────
    // 404 - 리소스 없음
    // ────────────────────────────────────────────────

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    // ────────────────────────────────────────────────
    // 400 - 잘못된 요청 (RuntimeException 계열 커스텀 예외)
    // ────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    // ────────────────────────────────────────────────
    // 500 - 기타 예외 (최후 방어선)
    // ────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
    }


    // ────────────────────────────────────────────────
    // ErrorResponse DTO (inner record)
    // ────────────────────────────────────────────────

    public record ErrorResponse(
            int status,
            String message,
            LocalDateTime timestamp
    ) {
        public static ErrorResponse of(int status, String message) {
            return new ErrorResponse(status, message, LocalDateTime.now());
        }
    }
}
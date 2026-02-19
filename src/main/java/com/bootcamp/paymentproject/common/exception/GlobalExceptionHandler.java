package com.bootcamp.paymentproject.common.exception;

import com.bootcamp.paymentproject.portone.exception.PortOneApiException;
import com.bootcamp.paymentproject.user.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENTS",
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        ) ;
        return ResponseEntity.badRequest().body(errorResponse);
    }


    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException e, HttpServletRequest request) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ErrorResponse.of(e.getErrorCode(), request.getRequestURI()));
    }

    @ExceptionHandler(PortOneApiException.class)
    public ResponseEntity<ErrorResponse> handlePortOneApiException(PortOneApiException e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getHttpStatus().value(),
                e.getErrorCode(),
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        ) ;
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }
}

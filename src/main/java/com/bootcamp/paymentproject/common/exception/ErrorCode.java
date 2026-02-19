package com.bootcamp.paymentproject.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Payment
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "존재하지 않는 결제 정보입니다.", HttpStatus.NOT_FOUND),
    POINT_INSUFFICIENT("POINT_INSUFFICIENT", "포인트 잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS_TRANSITION("INVALID_PAYMENT_STATUS_TRANSITION", "유효하지 않은 결제 상태 전이입니다.", HttpStatus.BAD_REQUEST),

    // PortOne (TODO 2)
    PORTONE_UNAUTHORIZED("PORTONE_UNAUTHORIZED", "PortOne 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    PORTONE_PAYMENT_NOT_FOUND("PORTONE_PAYMENT_NOT_FOUND", "PortOne에서 결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PORTONE_API_ERROR("PORTONE_API_ERROR", "PortOne API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    PORTONE_RESPONSE_NULL("PORTONE_RESPONSE_NULL", "PortOne 응답이 비어있습니다.", HttpStatus.BAD_GATEWAY),

    // Refund
    PAYMENT_NOT_REFUNDABLE("PAYMENT_NOT_REFUNDABLE", "환불 가능한 결제 상태가 아닙니다.", HttpStatus.CONFLICT),
    REFUND_NOT_FOUND("REFUND_NOT_FOUND", "존재하지 않는 환불 정보입니다.", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND),

    // Product
    PRODUCT_OUT_OF_STOCK("PRODUCT_OUT_OF_STOCK", "재고가 부족합니다.", HttpStatus.BAD_REQUEST),

    // Order
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "존재하지 않는 주문입니다.", HttpStatus.NOT_FOUND),

    // Membership
    USER_MEMBERSHIP_NOT_FOUND("USER_MEMBER_NOT_FOUND", "유저-멤버쉽 연관 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // User
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "중복된 이메일입니다", HttpStatus.CONFLICT),
    NOT_FOUND_USER("NOT_FOUND_USER", "존재하지 않는 유저입니다", HttpStatus.NOT_FOUND),
    NOT_FOUND_GRADE("NOT_FOUND_GRADE", "존재하지 않는 등급입니다", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

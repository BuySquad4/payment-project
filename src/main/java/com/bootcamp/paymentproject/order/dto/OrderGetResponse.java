package com.bootcamp.paymentproject.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderGetResponse {

    private String orderNumber;     // 사용자에게 보임
    private Long orderId;         // 시스템 식별자
    private BigDecimal totalAmount;        // 주문 금액
    private BigDecimal finalAmount;        // 포인트 사용 후 금액
    private BigDecimal earnedPoints;       // 적립 포인트
    private String currency;        // 금액(KRW)
    private String status;          // WAITING, COMPLETED, REFUNDED
    private LocalDateTime createdAt;
}
// orderNumber(String)    : 주문 번호(사용자에게 보임)
// orderId(String)        : 주문 ID(시스템 고유 식별자)
//	totalAmount(number)   : 주문 금액(포인트 차감 전)
//	finalAmount(number)   : 사용한 포인트
//	earnedPoints(number)  : 적립된 포인트
//	currency(String)      : 금액
//	status(String)        : 주문 상태(대기, 삭제 등)
//	createdAt(String)     : 주문 생성 시간
package com.bootcamp.paymentproject.order.dto;

import com.bootcamp.paymentproject.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderGetResponse {

    private String orderNumber;
    private Long orderId;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;     // 사용 포인트 usedPoint
    private BigDecimal earnedPoints;    // 적립 포인트 earnedPoint
    private String currency;            // KRW
    private String status;
    private LocalDateTime orderedAt;

    public OrderGetResponse(Order order, BigDecimal earnRate) {

        this.orderNumber = order.getOrderNumber();
        this.orderId = order.getId();
        this.totalAmount = order.getTotalPrice();

        BigDecimal usedPoint;
        // 사용할 포인트 검사
        if (order.getPointToUse() == null) {
            usedPoint = BigDecimal.ZERO;
        } else {
            usedPoint = order.getPointToUse();
        }

        this.finalAmount = usedPoint;

        // 적립 포인트 (포인트 사용 안 했을 때만)
        // 멤버십 적립 계산 NORMAL 1%, VIP 5%, HALF_VVIP 7%, VVIP 10%
        if (usedPoint.compareTo(BigDecimal.ZERO) == 0) {
            this.earnedPoints = order.getTotalPrice().multiply(earnRate);
        } else {
            this.earnedPoints = BigDecimal.ZERO;
        }

        this.currency = "KRW";
        this.status = order.getStatus().name();
        this.orderedAt = order.getOrderedAt();
    }
}
// orderNumber(String)    : 주문 번호(사용자에게 보임)
// orderId(String)        : 주문 ID(시스템 고유 식별자)
//	totalAmount(number)   : 주문 금액(포인트 차감 전)
//	finalAmount(number)   : 사용할 포인트
//	earnedPoints(number)  : 적립된 포인트
//	currency(String)      : 금액
//	status(String)        : 주문 상태(대기, 삭제 등)
//	createdAt(String)     : 주문 생성 시간
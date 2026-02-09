package com.bootcamp.paymentproject.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreateResponse {

    private String orderId;
    private String orderNumber;
    private int totalAmount;
}
//      orderId(String) : 생성된 주문 ID
//		totalAmount(number) : 서버에서 계산된 총 금액
//		orderNumber(String) : 주문 번호
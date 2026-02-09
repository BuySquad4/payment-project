package com.bootcamp.paymentproject.order.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreateRequest {
    private List<Item> items;

    @Getter
    public static class Item {
        private String productId;
        private int quantity;
    }
}
// items(array) : 주문 아이템 배열(상품명, 수량)
// - 상품ID로 조회 후 서버에서 상품 계산

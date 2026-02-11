package com.bootcamp.paymentproject.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreateRequest {
    @NotBlank(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    private List<Item> items;

    @Getter
    public static class Item {
        private String productId;
        private Long quantity;
    }
}
// items(array) : 주문 아이템 배열(상품명, 수량)
// 상품ID로 조회 후 서버에서 상품 계산

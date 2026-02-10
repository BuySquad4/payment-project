package com.bootcamp.paymentproject.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderProductQuantityDto {
    private final Long productId;
    private final Long quantity;
}

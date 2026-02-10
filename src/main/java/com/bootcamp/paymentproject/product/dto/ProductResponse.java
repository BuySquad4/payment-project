package com.bootcamp.paymentproject.product.dto;


import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.enums.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Long stock;
    private String description;
    private ProductStatus status;
    private String category;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .status(product.getStatus())
                .category(product.getCategory())

                .build();
    }
}

// @Build, from
//    private Long id;
//    private String name;
//    private BigDecimal price;
//    private Long stock;
//    private String description;
//    private ProductStatus status;
//    private String category;
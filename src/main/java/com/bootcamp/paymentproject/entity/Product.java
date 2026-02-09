package com.bootcamp.paymentproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String id;          // 1. ID
    private String name;        // 2. 상품명
    private Long price;         // 3. 판매가
    private Integer stock;      // 4. 재고 수량

    @Column(columnDefinition = "TEXT")
    private String description; // 5. 설명

    @Enumerated(EnumType.STRING)
    private ProductStatus status; // 6. 상태 (AVAILABLE, OUT_OF_STOCK 등)

    private String category;    // 7. 카테고리
}
package com.bootcamp.paymentproject.order.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "order_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @PositiveOrZero(message = "가격은 0 이상입니다.")
    private BigDecimal price;

    @Min(1)
    private Long stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderProduct(Product product, Long stock, Order order) {
        this.product = product;
        this.stock = stock;
        this.price = product.getPrice();
        this.name = product.getName();
        this.order = order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
//  id              : 주문 상품 ID
// name             : 주문 당시 상품명
// price            : 주문 당시 상품 가격
// stock            : 주문 당시 선택 수량(1개 이상)
//
// (FK)product_id   : 상품 ID - 연관
// (FK)order_id     : 주문 ID - 연관 - 이건 어디에 쓰는거지?
// 
// entity : 주문일/생성일
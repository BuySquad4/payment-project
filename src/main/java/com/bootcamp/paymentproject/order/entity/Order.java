package com.bootcamp.paymentproject.order.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_num")
    private String orderNumber;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;


    public Order(BigDecimal totalPrice) {
        this.orderNumber = "ORD-" + System.currentTimeMillis();
        this.totalPrice = totalPrice;
        this.status = OrderStatus.WAITING;
        this.orderedAt = LocalDateTime.now();
    }

    public void orderCompleted() {
        this.status = OrderStatus.COMPLETED;
    }
    public void orderRefunded() { this.status = OrderStatus.REFUNDED; }
}

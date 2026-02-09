package com.bootcamp.paymentproject.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String orderId;

    private String orderNumber;
    private String ordererEmail;

    private int totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderEnum status;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> items = new ArrayList<>();

    public Order(String ordererEmail) {
        this.orderId = UUID.randomUUID().toString();
        this.orderNumber = "ORD-" + System.currentTimeMillis();
        this.ordererEmail = ordererEmail;
        this.status = OrderEnum.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(OrderProduct item) {
        items.add(item);
        totalAmount += item.getPrice() * item.getQuantity();
    }
}
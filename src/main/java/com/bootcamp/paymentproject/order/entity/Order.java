package com.bootcamp.paymentproject.order.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.order.enums.OrderStatus;
import com.bootcamp.paymentproject.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "point_to_use")
    private BigDecimal pointToUse;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    // 주문1 : 상품N, 주문이 주인, 주문 저장 시 상품 저장, 주문에서 제거 시 주문상품도 제거)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void setUser(User user) { this.user = user; }

    // 생성자 오류 해결
    public static Order create() {
        Order order = new Order();
        order.orderNumber = "주문번호-" + System.currentTimeMillis();
        order.totalPrice = BigDecimal.ZERO;
        order.status = OrderStatus.WAITING;
        order.orderedAt = LocalDateTime.now();
        return order;
    }

    // 주문상품, 주문상품 총합
    public void OrderProductAdd(OrderProduct orderproduct) {
        orderproduct.setOrder(this);
        orderProducts.add(orderproduct);

        this.totalPrice = this.totalPrice.add(orderproduct.getPrice()
                .multiply(BigDecimal.valueOf(orderproduct.getStock())));
    }

    public void updatePointToUse(BigDecimal pointToUse) {
        if (pointToUse.compareTo(BigDecimal.ZERO) >= 0) {
            this.pointToUse = pointToUse;
        }
    }

    public void orderCompleted() {this.status = OrderStatus.COMPLETED;}
    public void orderPendingRefund(){
        this.status = OrderStatus.REFUND_PENDING;
    }
    public void orderRefunded(){this.status = OrderStatus.REFUNDED;}
    public void orderRefundFailed(){this.status = OrderStatus.REFUND_FAILED;}
}

// id           : 주문 ID(뭔 상품인지)
// orderNumer   : 주문번호
// totalPrice   : 주문 총금액        // OrderProduct.getPrice() : 주문 당시 가격
                                    // OrderProduct.getStock() : 주문 당시 수량
// status       : 주문 상태
// userPoint    : 사용 포인트
// user_id      : 사용자 ID
//
// 생성일과 수정일은 entity에
// orderedAt    : 주문일

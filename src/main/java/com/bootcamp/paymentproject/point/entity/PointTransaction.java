package com.bootcamp.paymentproject.point.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "pointTransactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal points;

    @Column(nullable = false, name = "remaining_points")
    private BigDecimal remainingPoints;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PointType type;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    @Column(name = "switch_to_type_earn_at")
    private LocalDateTime switchToTypeEarnAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public PointTransaction(BigDecimal points, PointType type, Order order) {
        this.points = points;
        this.type = type;
        this.user = order.getUser();
        this.order = order;

        this.remainingPoints = BigDecimal.ZERO;

        if(type == PointType.HOLDING){
            this.remainingPoints = points;
            this.expiredAt = LocalDateTime.now().plusWeeks(4);
            this.switchToTypeEarnAt = LocalDateTime.now().plusWeeks(2);
        }
    }

    public void updateType(PointType type) {
        this.type = type;
    }

    public void updateRemainingPoints(BigDecimal remainingPoints) {
        if(remainingPoints.compareTo(BigDecimal.ZERO) >= 0 && remainingPoints.compareTo(points) <= 0){
            this.remainingPoints = remainingPoints;
        }

    }

    private static PointTransaction of(User user,  Order order,  PointType type,  BigDecimal points, BigDecimal remainingPoints, LocalDateTime expiredAt) {
        PointTransaction tx = new PointTransaction();
        tx.user = user;
        tx.order = order;
        tx.type = type;
        tx.points = points;
        tx.remainingPoints = remainingPoints;
        tx.expiredAt = expiredAt;
        return tx;
    }

    public static PointTransaction spend(User user, Order order, BigDecimal amountToSpend) {
        BigDecimal negative = amountToSpend.abs().negate();
        return of(user, order, PointType.SPENT, negative, BigDecimal.ZERO, null);
    }

    public static PointTransaction cancel(User user, Order order, BigDecimal deltaPoints) {
        return of(user, order, PointType.CANCEL, deltaPoints, BigDecimal.ZERO, null);
    }
}

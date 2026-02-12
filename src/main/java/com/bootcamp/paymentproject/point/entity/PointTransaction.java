package com.bootcamp.paymentproject.point.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Entity
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

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

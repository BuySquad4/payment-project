package com.bootcamp.paymentproject.point.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
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

    @Column(name = "remaining_points")
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

        // order가 null일 수도 있게 변경
        this.order = order;
        this.user = (order != null) ? order.getUser() : null;

        // HOLDING이 아닌 트랜잭션의 remainingPoints 기본값을 0으로 초기화 (null 방지)
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

    // PointTransaction 공통 생성 메서드
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

    // 포인트 취소/상쇄 트랜잭션 생성 (CANCEL, 부호 그대로 사용)
    public static PointTransaction cancel(User user, Order order, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ServiceException(ErrorCode.INVALID_POINT_AMOUNT);
        }
        return of(user, order, PointType.CANCEL, amount, BigDecimal.ZERO, null);
    }

    // 사용 포인트 복구 (+금액으로 CANCEL 생성)
    public static PointTransaction cancelSpendRestore(User user, Order order, BigDecimal restoreAmount) {
        if (restoreAmount == null || restoreAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.INVALID_POINT_AMOUNT);
        }
        return cancel(user, order, restoreAmount.abs());
    }

    // HOLDING 포인트 무효화 (-금액으로 CANCEL 생성)
    public static PointTransaction cancelHolding(User user, Order order, BigDecimal holdingAmount) {
        if (holdingAmount == null || holdingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.INVALID_POINT_AMOUNT);
        }
        return cancel(user, order, holdingAmount.abs().negate());
    }
}

package com.bootcamp.paymentproject.payment.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String paymentId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public Payment(String paymentId, BigDecimal amount, Order order) {
        this.paymentId = paymentId;
        this.amount = amount;
        status = PaymentStatus.PENDING;
        this.order = order;
    }

    public void paymentFailed() {
        if(this.status.canTransitToTargetStatus(PaymentStatus.FAILED)) {
            this.status = PaymentStatus.FAILED;
        }
    }

    public void paymentCanceled() {
        if(this.status.canTransitToTargetStatus(PaymentStatus.CANCELED)) {
            this.status = PaymentStatus.CANCELED;
        }
    }

    public void paymentConfirmed() {
        if(this.status.canTransitToTargetStatus(PaymentStatus.APPROVED)) {
            this.status = PaymentStatus.APPROVED;
        }
    }
}

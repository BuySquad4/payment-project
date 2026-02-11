package com.bootcamp.paymentproject.refund.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.refund.enums.RefundState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Entity
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private String reason;

    @Enumerated(EnumType.STRING)
    private RefundState state;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;

    public Refund(BigDecimal amount, String reason, RefundState state, LocalDateTime refundedAt, Payment payment) {
        this.amount = amount;
        this.reason = reason;
        this.state = state;
        this.refundedAt = refundedAt;
        this.payment = payment;
    }

    public void updateToTargetState(RefundState state) {
        this.state = state;
    }

    public void updateRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
}

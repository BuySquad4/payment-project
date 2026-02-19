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

    @Column(unique = true, nullable = false)
    private String paymentId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /**
     * 환불 가능 마감 시각 (결제 승인 시각 + 2주)
     * - now.isAfter(refundableUntil) 이면 환불 불가
     */
    @Column(name = "refundable_until")
    private LocalDateTime refundableUntil;

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

    public void paymentRefunded() {
        if(this.status.canTransitToTargetStatus(PaymentStatus.REFUNDED)) {
            this.status = PaymentStatus.REFUNDED;
        }
    }

    public void paymentRefundFailed(){
        if(this.status.canTransitToTargetStatus(PaymentStatus.REFUND_FAILED)) {
            this.status = PaymentStatus.REFUND_FAILED;
        }
    }

    /** 현재 시각 기준 환불 가능 여부 확인 */
    public boolean isRefundable(LocalDateTime now) {
        return refundableUntil != null && !now.isAfter(refundableUntil);
    }

    /** 결제 승인 처리 (APPROVED, paidAt, refundableUntil 설정) */
    public void approve(LocalDateTime approvedAt) {
        paymentConfirmed();                 // status -> APPROVED
        this.paidAt = approvedAt;           // 승인 시각 기록
        this.refundableUntil = approvedAt.plusDays(14); // 환불 가능 마감(2주)
    }

    /** 결제 환불 처리 (REFUNDED, refundedAt 설정) */
    public void refund(LocalDateTime refundedAt) {
        paymentRefunded();                 // status -> REFUNDED
        this.refundedAt = refundedAt;      // 환불 시각 기록
    }
}

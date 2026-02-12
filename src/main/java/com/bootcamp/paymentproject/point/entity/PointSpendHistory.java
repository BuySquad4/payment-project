package com.bootcamp.paymentproject.point.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@Entity
@Table(name = "point_spend_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointSpendHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "earn_transaction")
    private PointTransaction earnTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spend_transaction")
    private PointTransaction spendTransaction;

    private BigDecimal amount;

    public PointSpendHistory(PointTransaction earnTransaction, PointTransaction spendTransaction, BigDecimal amount) {
        this.earnTransaction = earnTransaction;
        this.spendTransaction = spendTransaction;
        this.amount = amount;
    }
}

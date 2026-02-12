package com.bootcamp.paymentproject.membership.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal earnRate;
    @Column(nullable = false, unique = true)
    private String gradeName;

    @Column(name = "min_total_paid_amount", nullable = false)
    private BigDecimal MinTotalPaidAmount;
}

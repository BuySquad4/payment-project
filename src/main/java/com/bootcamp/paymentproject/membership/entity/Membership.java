package com.bootcamp.paymentproject.membership.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.membership.enums.MembershipGrade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal earnRate;
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private MembershipGrade gradeName;

    @Column(name = "min_total_paid_amount", nullable = false)
    private BigDecimal minTotalPaidAmount;

    @OneToMany(mappedBy = "membership", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMembership> userMemberships = new ArrayList<>();
}

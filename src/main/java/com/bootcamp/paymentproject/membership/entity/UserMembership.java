package com.bootcamp.paymentproject.membership.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.membership.enums.MembershipGrade;
import com.bootcamp.paymentproject.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "user_memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    public void updateTotalAmount(BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) >= 0) {
            this.totalAmount = totalAmount;
        }
    }

    public void updateMembership(Membership membership) {
        if (membership != null) {
            this.membership = membership;
        }
    }
}

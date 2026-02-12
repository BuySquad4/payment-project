package com.bootcamp.paymentproject.membership.repository;

import com.bootcamp.paymentproject.membership.entity.Membership;
import com.bootcamp.paymentproject.membership.enums.MembershipGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByGradeName(MembershipGrade gradeName);
}

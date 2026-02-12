package com.bootcamp.paymentproject.membership.repository;

import com.bootcamp.paymentproject.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
}

package com.bootcamp.paymentproject.membership.repository;

import com.bootcamp.paymentproject.membership.entity.Membership;
import com.bootcamp.paymentproject.membership.entity.UserMembership;
import com.bootcamp.paymentproject.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    Optional<UserMembership> findByUser(User user);
}

package com.bootcamp.paymentproject.membership.repository;

import com.bootcamp.paymentproject.membership.entity.Membership;
import com.bootcamp.paymentproject.membership.entity.UserMembership;
import com.bootcamp.paymentproject.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    Optional<UserMembership> findByUser(User user);

    @Query("SELECT m.earnRate, 0 FROM UserMembership um join um.membership m WHERE um.user.id = :userId")
    Optional<BigDecimal> findEarnRateByUserId(@Param("userId") Long userId);
}

package com.bootcamp.paymentproject.membership.repository;

import com.bootcamp.paymentproject.membership.entity.Membership;
import com.bootcamp.paymentproject.membership.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<Membership, Long> {
    @Query("select um " +
            "from UserMembership um " +
            "join fetch um.membership where um.user.id = :userId") // um에서 userId에 해당하는 membership 가져옴
    Optional<UserMembership> findByUserId(@Param("userId") Long userId);

}

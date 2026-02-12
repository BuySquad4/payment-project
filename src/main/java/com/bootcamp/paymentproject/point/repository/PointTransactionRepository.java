package com.bootcamp.paymentproject.point.repository;

import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    @Query("SELECT COALESCE(sum(p.remainingPoints), 0) FROM PointTransaction p WHERE p.user.id = :userId AND p.type = :type")
    BigDecimal getPointSumByUserId(@Param("userId") Long userId, @Param("type") PointType type);
}

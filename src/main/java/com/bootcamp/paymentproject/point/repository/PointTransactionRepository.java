package com.bootcamp.paymentproject.point.repository;

import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    @Query("SELECT COALESCE(sum(p.remainingPoints), 0) FROM PointTransaction p WHERE p.user.id = :userId AND p.type = :type")
    BigDecimal getPointSumByUserId(@Param("userId") Long userId, @Param("type") PointType type);

    // earnAt 대신 createdAt(생성일) 기준으로 적립 대기 건 조회
    List<PointTransaction> findAllByTypeAndCreatedAtBefore(PointType type, LocalDateTime dateTime);

    // ExpireAt -> ExpiredAt (엔티티 필드명과 일치)
    List<PointTransaction> findAllByTypeAndExpiredAtBefore(PointType type, LocalDateTime dateTime);
}
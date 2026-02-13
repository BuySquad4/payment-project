package com.bootcamp.paymentproject.point.repository;

import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    @Query("SELECT COALESCE(sum(p.remainingPoints), 0) " +
            "FROM PointTransaction p " +
            "WHERE p.user.id = :userId AND p.type = :type")
    BigDecimal getPointSumByUserId(@Param("userId") Long userId, @Param("type") PointType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pt " +
            "FROM PointTransaction pt " +
            "WHERE pt.type = :type " +
            "AND pt.user.id = :userId " +
            "AND pt.remainingPoints > 0 " +
            "AND pt.expiredAt > CURRENT_TIMESTAMP " +
            "ORDER BY pt.expiredAt ASC, pt.id ASC")
    List<PointTransaction> findEarnTransactionsByUserID(@Param("userId") Long userId, @Param("type") PointType type);

    // ExpireAt -> ExpiredAt (엔티티 필드명과 일치)
    List<PointTransaction> findAllByTypeAndExpiredAtBefore(PointType type, LocalDateTime dateTime);

    // Type 뒤에 'And'를 붙여서 두 필드를 확실히 구분해줘야 합니다.
    List<PointTransaction> findAllByTypeAndSwitchToTypeEarnAtBefore(PointType type, LocalDateTime dateTime);

    Optional<PointTransaction> findFirstByUser_IdAndOrder_IdAndType(
            Long userId, Long orderId, PointType type
    );

    List<PointTransaction> findAllByUser_IdAndOrder_IdAndTypeIn(
            Long userId, Long orderId, List<PointType> types
    );
}

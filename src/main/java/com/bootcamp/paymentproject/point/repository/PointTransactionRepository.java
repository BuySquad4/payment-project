package com.bootcamp.paymentproject.point.repository;

import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    @Query("SELECT COALESCE(sum(p.remainingPoints), 0) FROM PointTransaction p WHERE p.user.id = :userId AND p.type = :type")
    BigDecimal getPointSumByUserId(@Param("userId") Long userId, @Param("type") PointType type);

    // ExpireAt -> ExpiredAt (엔티티 필드명과 일치)
    List<PointTransaction> findAllByTypeAndExpiredAtBefore(PointType type, LocalDateTime dateTime);

    List<PointTransaction> findAllByTypeSwitchToTypeEarnAtBefore(PointType pointType, LocalDateTime now);

    /**
     * 환불 시: 해당 주문의 사용(SPENT) 트랜잭션 찾기
     */
    Optional<PointTransaction> findFirstByUserIdAndOrderIdAndType(Long userId, Long orderId, PointType type);

    /**
     * 환불 시: 해당 주문의 적립(EARN/HOLDING) 트랜잭션들 찾기
     */
    List<PointTransaction> findAllByUserIdAndOrderIdAndTypeIn(Long userId, Long orderId, List<PointType> types);
}

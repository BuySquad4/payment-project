package com.bootcamp.paymentproject.payment.repository;

import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment> findByOrderId(Long orderId);

    // 해당 조회 발생 시 다른 confirm 요청은 해당 row 수정 불가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findByPaymentId(@Param("paymentId") String paymentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0)  FROM Payment p WHERE p.order.user.id = :userId AND p.status = :status")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId, @Param("status") PaymentStatus paymentStatus);
}

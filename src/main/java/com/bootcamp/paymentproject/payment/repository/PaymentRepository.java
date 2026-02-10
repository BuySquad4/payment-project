package com.bootcamp.paymentproject.payment.repository;

import com.bootcamp.paymentproject.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    // 해당 조회 발생 시 다른 confirm 요청은 해당 row 수정 불가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findByPaymentId(@Param("paymentId") String paymentId);
}

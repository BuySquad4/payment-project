package com.bootcamp.paymentproject.refund.repository;

import com.bootcamp.paymentproject.refund.entity.Refund;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Refund r where r.payment.paymentId = :paymentId")
    Optional<Refund> findByPaymentId(@Param("paymentId") String paymentId);
}

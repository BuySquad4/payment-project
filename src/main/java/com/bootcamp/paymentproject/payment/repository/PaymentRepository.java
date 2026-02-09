package com.bootcamp.paymentproject.payment.repository;

import com.bootcamp.paymentproject.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}

package com.bootcamp.paymentproject.point.repository;

import com.bootcamp.paymentproject.point.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<
        PointTransaction, Long> {
}

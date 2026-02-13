package com.bootcamp.paymentproject.point.repository;

import com.bootcamp.paymentproject.point.entity.PointSpendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointSpendHistoryRepository extends JpaRepository<PointSpendHistory,Long> {
}

package com.bootcamp.paymentproject.point.repository;

import com.bootcamp.paymentproject.point.entity.PointSpendHistory;
import com.bootcamp.paymentproject.point.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointSpendHistoryRepository extends JpaRepository<PointSpendHistory,Long> {

    List<PointSpendHistory> findAllBySpendTransaction(PointTransaction spendTransaction);
}

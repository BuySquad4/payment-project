package com.bootcamp.paymentproject.order.Repository;

import com.bootcamp.paymentproject.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}

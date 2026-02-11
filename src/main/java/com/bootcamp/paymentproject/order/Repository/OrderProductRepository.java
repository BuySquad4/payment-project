package com.bootcamp.paymentproject.order.Repository;

import com.bootcamp.paymentproject.order.dto.OrderProductQuantityDto;
import com.bootcamp.paymentproject.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct,Long> {

    @Query("SELECT new com.bootcamp.paymentproject.order.dto.OrderProductQuantityDto(op.product.id, op.stock) " +
            "FROM OrderProduct op " +
            "WHERE op.order.id = :orderId")
    List<OrderProductQuantityDto> findByOrderId(@Param("orderId") Long orderId);
}


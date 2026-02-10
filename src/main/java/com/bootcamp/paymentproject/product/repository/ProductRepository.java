package com.bootcamp.paymentproject.product.repository;

import com.bootcamp.paymentproject.product.entity.Product; // 주소에 .entity 추가!
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p Where p.id IN :productIds")
    List<Product> findAllByIdIn(@Param("productIds") List<Long> productIdList);
}
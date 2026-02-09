package com.bootcamp.paymentproject.product.repository;

import com.bootcamp.paymentproject.product.entity.Product; // 주소에 .entity 추가!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
}
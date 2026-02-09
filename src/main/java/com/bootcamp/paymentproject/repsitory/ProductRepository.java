package com.bootcamp.paymentproject.repository;

import com.bootcamp.paymentproject.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
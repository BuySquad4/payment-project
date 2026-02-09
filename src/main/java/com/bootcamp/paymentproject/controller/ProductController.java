package com.bootcamp.paymentproject.controller;

import com.bootcamp.paymentproject.entity.Product;
import com.bootcamp.paymentproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        // DB에 데이터가 없을 때를 대비해 가짜 데이터를 미리 좀 넣어줄까요?
        // 실제로는 DB에 있는 데이터를 가져옵니다.
        return productRepository.findAll();
    }
}
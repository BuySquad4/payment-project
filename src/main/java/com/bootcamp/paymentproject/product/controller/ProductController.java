package com.bootcamp.paymentproject.product.controller;

import com.bootcamp.paymentproject.common.dto.SuccessResponse; // 👈 방금 만든 DTO 임포트
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
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
    public SuccessResponse<List<Product>> getAllProducts() {
        // 1. DB에서 상품 목록을 가져옵니다.
        List<Product> products = productRepository.findAll();

        // 2. 그냥 보내지 말고 ApiResponse.success() 봉투에 담아서 보냅니다.
        return SuccessResponse.success(products,"주문이 성공적으로 조회했습니다");
    }
}
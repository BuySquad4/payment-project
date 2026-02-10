package com.bootcamp.paymentproject.product.controller;

import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.service.ProductService; // Service 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService; // repository 대신 service의존

    @GetMapping
    public ResponseEntity<SuccessResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(SuccessResponse.success(products, "상품 목록 조회가 완료되었습니다."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<Product>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(SuccessResponse.success(product, "상품 상세 조회가 완료되었습니다."));
    }
}
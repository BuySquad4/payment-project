package com.bootcamp.paymentproject.product.controller;

import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.product.dto.ProductResponse;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> responses = productService.getAllProducts()
                .stream()
                .map(ProductResponse::from)
                .toList();

        return ResponseEntity.ok(SuccessResponse.success(responses, "상품 목록 조회가 완료되었습니다."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        ProductResponse response = ProductResponse.from(product);

        return ResponseEntity.ok(SuccessResponse.success(response, "상품 상세 조회가 완료되었습니다."));
    }
}
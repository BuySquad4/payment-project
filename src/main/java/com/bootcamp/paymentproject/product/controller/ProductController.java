package com.bootcamp.paymentproject.product.controller;

import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.product.dto.ProductResponse;
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

        return ResponseEntity.ok(SuccessResponse.success(
                productService.getAllProducts(),
                "상품 목록 조회가 완료되었습니다."
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProductResponse>> getProductById(@PathVariable Long id) {

        return ResponseEntity.ok(SuccessResponse.success(
                productService.getProductById(id),
                "상품 상세 조회가 완료되었습니다."
        ));
    }
}
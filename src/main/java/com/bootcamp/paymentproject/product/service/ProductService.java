package com.bootcamp.paymentproject.product.service;

import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.exception.ProductErrorCode;
import com.bootcamp.paymentproject.product.exception.ProductException;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 성능 최적화
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 전체 상품 목록 조회
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 단건 상품 조회
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
package com.bootcamp.paymentproject.product.service;

import com.bootcamp.paymentproject.product.dto.ProductResponse;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.enums.ProductStatus;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .stock(50L)
                .description("테스트 상품 설명입니다.")
                .status(ProductStatus.AVAILABLE)
                .category("전자기기")
                .build();
    }

    // ────────────────────────────────────────────────
    // 전체 조회
    // ────────────────────────────────────────────────

    @Test
    void 상품_전체조회_성공() {
        // given
        given(productRepository.findAll())
                .willReturn(List.of(product));

        // when
        List<ProductResponse> result = productService.getAllProducts();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("테스트 상품");
        assertThat(result.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));

        then(productRepository).should().findAll();
    }

    @Test
    void 상품_전체조회_빈목록() {
        // given
        given(productRepository.findAll())
                .willReturn(List.of());

        // when
        List<ProductResponse> result = productService.getAllProducts();

        // then
        assertThat(result).isEmpty();
    }

    // ────────────────────────────────────────────────
    // 단건 조회
    // ────────────────────────────────────────────────

    @Test
    void 상품_단건조회_성공() {
        // given
        given(productRepository.findById(1L))
                .willReturn(Optional.of(product));

        // when
        ProductResponse response = productService.getProductById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getName()).isEqualTo("테스트 상품");
        assertThat(response.getStock()).isEqualTo(50L);
        assertThat(response.getStatus()).isEqualTo(ProductStatus.AVAILABLE);
        assertThat(response.getCategory()).isEqualTo("전자기기");

        then(productRepository).should().findById(1L);
    }

    @Test
    void 상품_단건조회_실패_존재하지않는ID() {
        // given
        given(productRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("해당 상품을 찾을 수 없습니다. ID: 999");

        then(productRepository).should().findById(999L);
    }
}
package com.bootcamp.paymentproject.product.controller;

import com.bootcamp.paymentproject.common.security.CustomUserDetailsService;
import com.bootcamp.paymentproject.common.security.JwtTokenProvider;
import com.bootcamp.paymentproject.config.TestSecurityConfig;
import com.bootcamp.paymentproject.product.dto.ProductResponse;
import com.bootcamp.paymentproject.product.enums.ProductStatus;
import com.bootcamp.paymentproject.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // AuthControllerTest와 동일하게 직접 생성 (TestSecurityConfig에 ObjectMapper 빈 없음)
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id("1")
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
    void 상품_전체조회_성공시_200_반환() throws Exception {
        // given
        given(productService.getAllProducts())
                .willReturn(List.of(productResponse));

        // when & then
        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].name").value("테스트 상품"))
                .andExpect(jsonPath("$.data[0].price").value(10000))
                .andExpect(jsonPath("$.data[0].stock").value(50))
                .andExpect(jsonPath("$.message").value("상품 목록 조회가 완료되었습니다."));
    }

    @Test
    void 상품_전체조회_빈목록_200_반환() throws Exception {
        // given
        given(productService.getAllProducts())
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ────────────────────────────────────────────────
    // 단건 조회
    // ────────────────────────────────────────────────

    @Test
    void 상품_단건조회_성공시_200_반환() throws Exception {
        // given
        given(productService.getProductById(1L))
                .willReturn(productResponse);

        // when & then
        mockMvc.perform(get("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.stock").value(50))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.category").value("전자기기"))
                .andExpect(jsonPath("$.message").value("상품 상세 조회가 완료되었습니다."));
    }

    @Test
    void 상품_단건_조회_실패_없는_ID() throws Exception {
        // given
        given(productService.getProductById(999L))
                .willThrow(new EntityNotFoundException("해당 상품을 찾을 수 없습니다. ID: 999"));

        // when & then
        mockMvc.perform(get("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
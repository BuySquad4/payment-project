package com.bootcamp.paymentproject.order.controller;

import com.bootcamp.paymentproject.common.security.CustomUserDetails;
import com.bootcamp.paymentproject.order.repository.OrderRepository;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private CustomUserDetails customUserDetails;

    //인증
    @BeforeEach
    void setup() {
        User admin = userRepository.findByEmail("admin@test.com")
                .orElseThrow(() -> new RuntimeException("계정없음"));

        customUserDetails = new CustomUserDetails(admin);
    }

    @Test
    void 주문_생성_성공() throws Exception {
        Map<String, Object> item =
                Map.of(
                        "productId", "1",
                        "quantity", 2
                );

        Map<String, Object> request =
                Map.of(
                        "items", List.of(item)
                );

        mockMvc.perform(post("/api/orders")
                        .with(user(customUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }

    @Test
    void 주문_전체_조회_성공() throws Exception {

        mockMvc.perform(get("/api/orders")
                        .with(user(customUserDetails))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 주문_상세_조회_성공() throws Exception {

        // 먼저 주문 하나 생성
        Map<String, Object> item =
                Map.of(
                        "productId", "1",
                        "quantity", 1
                );

        Map<String, Object> request =
                Map.of(
                        "items", List.of(item)
                );

        mockMvc.perform(post("/api/orders")
                        .with(user(customUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        // 생성된 주문 ID 가져오기
        Long orderId = orderRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/orders/" + orderId)
                        .with(user(customUserDetails))
                )
                .andExpect(status().isOk());
    }
}
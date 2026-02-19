package com.bootcamp.paymentproject.order.controller;

import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.common.security.CustomUserDetails;
import com.bootcamp.paymentproject.order.dto.OrderCreateRequest;
import com.bootcamp.paymentproject.order.dto.OrderCreateResponse;
import com.bootcamp.paymentproject.order.dto.OrderGetResponse;
import com.bootcamp.paymentproject.order.service.OrderService;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;


    // 주문 생성
    @PostMapping
    public ResponseEntity<SuccessResponse<OrderCreateResponse>> createOrder(
            @RequestBody OrderCreateRequest request,
            Authentication authentication
    ) {
        CustomUserDetails principal =
                (CustomUserDetails) authentication.getPrincipal();

        String email = principal.getEmail();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success(
                        orderService.createOrder(request, email),
                        "주문 생성 완료"
                ));
    }

    // 주문 조회 :
    @GetMapping
    public ResponseEntity<SuccessResponse<List<OrderGetResponse>>> getOrders() {

        List<OrderGetResponse> responses = orderService.getAllOrders();

        return ResponseEntity.ok(
                SuccessResponse.success(responses, "주문 목록 조회 성공")
        );
    }

    // 주문 상세 조회 : 이거 단건 아님. id 는 해당 주문자의 id?
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<OrderGetResponse>> getOrder(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                SuccessResponse.success(orderService.getOrder(id), "주문 조회 완료")
        );
    }

}
package com.bootcamp.paymentproject.order.Controller;

import com.bootcamp.paymentproject.order.Repository.OrderRepository;
import com.bootcamp.paymentproject.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    // 주문 생성
    @PostMapping
    public Order createOrder(
            @RequestBody Order order
    ) {
        return orderRepository.save(order);
    }

    // 주문 조회
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }


}
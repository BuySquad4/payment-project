package com.bootcamp.paymentproject.order.service;

import com.bootcamp.paymentproject.order.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
}

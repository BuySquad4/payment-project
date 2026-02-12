package com.bootcamp.paymentproject.order.service;

import com.bootcamp.paymentproject.membership.repository.UserMembershipRepository;
import com.bootcamp.paymentproject.order.Repository.OrderRepository;
import com.bootcamp.paymentproject.order.dto.OrderCreateRequest;
import com.bootcamp.paymentproject.order.dto.OrderCreateResponse;
import com.bootcamp.paymentproject.order.dto.OrderGetResponse;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.entity.OrderProduct;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j // 로그를 찍기 위해 추가했습니다.
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserMembershipRepository userMembershipRepository;

    // 주문 생성
    @Transactional
    public OrderCreateResponse createOrder(
            OrderCreateRequest request,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음")
        );

        Order order = Order.create();
        order.setUser(user); // 포인트 확인용 user

        // 요청 시 재고 확인
        for (OrderCreateRequest.Item item : request.getItems()) {
            Product product = productRepository.findById(Long.valueOf(item.getProductId()))
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            if (product.getStock() < item.getQuantity())
                throw new RuntimeException("재고 부족");

            OrderProduct op = new OrderProduct(product, (Long)item.getQuantity(), order);
            order.OrderProductAdd(op);
        }

        orderRepository.save(order);

        return new OrderCreateResponse(
                order.getId().toString(),
                order.getOrderNumber(),
                order.getTotalPrice().intValue()
        );
    }

    // 주문 조회
    @Transactional(readOnly = true)
    public List<OrderGetResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public OrderGetResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        BigDecimal earnRate = userMembershipRepository
                .findEarnRateByUserId(order.getUser().getId())
                .orElse(BigDecimal.ZERO);

        return new OrderGetResponse(order, earnRate);
    }

    private OrderGetResponse toResponse(Order order) {

        BigDecimal earnRate = userMembershipRepository
                .findEarnRateByUserId(order.getUser().getId())
                .orElse(BigDecimal.ZERO);

        return new OrderGetResponse(order, earnRate);
    }
    private BigDecimal getMembershipRate(User user) {
        return userMembershipRepository.findEarnRateByUserId(user.getId())
                .orElse(BigDecimal.ZERO);  // 멤버십 없으면 0%
    }
}

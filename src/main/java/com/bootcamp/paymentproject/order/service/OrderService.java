package com.bootcamp.paymentproject.order.service;

import com.bootcamp.paymentproject.order.Repository.OrderRepository;
import com.bootcamp.paymentproject.order.dto.OrderCreateRequest;
import com.bootcamp.paymentproject.order.dto.OrderCreateResponse;
import com.bootcamp.paymentproject.order.dto.OrderGetResponse;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.entity.OrderProduct;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그용 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j // 로그를 찍기 위해 추가했습니다.
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // 주문 생성
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        Order order = Order.create();

        for (OrderCreateRequest.Item item : request.getItems()) {
            Product product = productRepository.findById(Long.valueOf(item.getProductId()))
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            // [수정] 프론트에서 0이 넘어오더라도 최소 1로 보정합니다.
            long orderStock = Math.max(1L, (long) item.getStock());

            // 데이터가 제대로 안 들어왔을 때 범인을 찾기 위해 로그를 남깁니다.
            if (item.getStock() <= 0) {
                log.warn("⚠️ 프론트에서 수량이 0 이하로 넘어왔습니다! (보정 전: {}, 상품ID: {})", item.getStock(), item.getProductId());
            }

            if (product.getStock() < orderStock)
                throw new RuntimeException("재고 부족");

            // 보정된 orderStock을 사용합니다.
            OrderProduct op = new OrderProduct(product, orderStock, order);
            order.OrderProductAdd(op);
        }

        orderRepository.save(order);

        return new OrderCreateResponse(
                order.getId().toString(),
                order.getOrderNumber(),
                order.getTotalPrice().intValue()
        );
    }

    // ... 아래 조회 로직들은 기존과 동일하므로 생략 (그대로 두시면 됩니다)

    @Transactional(readOnly = true)
    public List<OrderGetResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderGetResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));
        return new OrderGetResponse(
                order.getOrderNumber(), order.getId().toString(),
                order.getTotalPrice().intValue(), order.getTotalPrice().intValue(),
                0, "KRW", order.getStatus().name(), order.getOrderedAt()
        );
    }

    private OrderGetResponse toResponse(Order order) {
        return new OrderGetResponse(
                order.getOrderNumber(), order.getId().toString(),
                order.getTotalPrice().intValue(), order.getTotalPrice().intValue(),
                order.getTotalPrice().intValue() / 10, "KRW",
                order.getStatus().name(), order.getCreatedAt()
        );
    }
}
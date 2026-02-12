package com.bootcamp.paymentproject.order.service;

import com.bootcamp.paymentproject.common.security.CustomUserDetails;
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
import org.springframework.security.core.Authentication; // user 누적 포인트 용도
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
                .orElseThrow(() -> new RuntimeException("주문 없음")
        );

        return new OrderGetResponse(
                order.getOrderNumber(),
                order.getId(),
                order.getTotalPrice(),
                order.getTotalPrice(),
                BigDecimal.ZERO,
                "KRW",
                order.getStatus().name(),
                order.getOrderedAt()
        );
    }

    private OrderGetResponse toResponse(Order order) {

        BigDecimal usedPoint;

        // 사용할 포인트 검사
        if (order.getPointToUse() == null) {
            usedPoint = BigDecimal.ZERO;
        } else {
            usedPoint = order.getPointToUse();
        }

        // 적립 포인트 초기화
        BigDecimal earnedPoint = BigDecimal.ZERO;

        // 포인트를 사용하지 않은 경우에만 적립 계산
        if (usedPoint.compareTo(BigDecimal.ZERO) == 0) {

            User user = order.getUser();

            // 멤버십 적립 계산 NORMAL 1%, VIP 5%, half-VVIP 7%, VVIP 10%
            // 유저 테이블에서 ID로 멤버십 테이블의 등급에 따른 퍼센트를 가져온다고 설정
            BigDecimal rate = getMembershipRate(order.getUser());
            earnedPoint = order.getTotalPrice().multiply(rate);

         }

        return new OrderGetResponse(
                order.getOrderNumber(),
                order.getId(),
                order.getTotalPrice(),  // 주문 총 가격
                usedPoint,              // 사용 포인트
                earnedPoint,            // 등급별 적립 포인트
                "KRW",
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }

    private BigDecimal getMembershipRate(User user) {

        return userMembershipRepository.findByUserId(user.getId())
                .map(userMembership -> userMembership.getMembership().getEarnRate())
                .orElse(BigDecimal.ZERO);  // 멤버십 없으면 0%
    }
}

package com.bootcamp.paymentproject.order.service;

import com.bootcamp.paymentproject.membership.repository.UserMembershipRepository;
import com.bootcamp.paymentproject.order.dto.OrderCreateRequest;
import com.bootcamp.paymentproject.order.dto.OrderCreateResponse;
import com.bootcamp.paymentproject.order.dto.OrderGetResponse;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.repository.OrderRepository;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.repository.UserRepository;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("admin@test.com")
                .username("User")
                .password("12345678")
                .phone("01012345678")
                .pointBalance(BigDecimal.ZERO)
                .build();

        product = Product.builder()
                .id(100L)
                .name("상품A")
                .price(BigDecimal.valueOf(1000))
                .stock(10L)
                .build();
    }

    // 주문 생성 성공
    @Test
    void 주문_생성_성공() {
        OrderCreateRequest.Item item =
                new OrderCreateRequest.Item("100", 2L);

        OrderCreateRequest request = new OrderCreateRequest();
        try {
            var field = OrderCreateRequest.class.getDeclaredField("items");
            field.setAccessible(true);
            field.set(request, List.of(item));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(user));

        given(productRepository.findById(100L))
                .willReturn(Optional.of(product));

        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> {
                Order saved = invocation.getArgument(0);

                // 안될경우
                var idField = Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(saved, 1L);

                return saved;
            });

        OrderCreateResponse response =
                orderService.createOrder(request, "admin@test.com");

        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualTo(2000);

        then(orderRepository).should().save(any(Order.class));
    }

    // 주문 생성 - 예외_재고
    @Test
    void 주문_생성_실패_재고부족() {
        // given
        product.setStock(1L);

        OrderCreateRequest.Item item =
                new OrderCreateRequest.Item("100", 5L);

        OrderCreateRequest request = new OrderCreateRequest();
        try {
            var field = OrderCreateRequest.class.getDeclaredField("items");
            field.setAccessible(true);
            field.set(request, List.of(item));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(user));

        given(productRepository.findById(100L))
                .willReturn(Optional.of(product));
        
        assertThatThrownBy(() ->
                orderService.createOrder(request, "admin@test.com")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("재고 부족");
    }

    // 주문 조회
    @Test
    void 주문_전체조회_성공() {
        Order order = Order.create();
        order.setUser(user);

        given(orderRepository.findAll())
                .willReturn(List.of(order));

        given(userMembershipRepository.findEarnRateByUserId(any()))
                .willReturn(Optional.of(BigDecimal.valueOf(0.01)));

        List<OrderGetResponse> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isNotNull();
    }


    // 주문상세조회
    @Test
    void 주문_상세_조회_성공() {
        Order order = Order.create();
        order.setUser(user);

        given(orderRepository.findById(1L))
                .willReturn(Optional.of(order));

        given(userMembershipRepository.findEarnRateByUserId(any()))
                .willReturn(Optional.of(BigDecimal.valueOf(0.05)));

        OrderGetResponse response = orderService.getOrder(1L);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(order.getId());
    }


    // 주문 상세 조회 실패
    @Test
    void 주문_상세_조회_실패() {
        given(orderRepository.findById(any()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.getOrder(1L)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("주문 없음");
    }
}

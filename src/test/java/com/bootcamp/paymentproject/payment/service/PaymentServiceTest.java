package com.bootcamp.paymentproject.payment.service;

import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.repository.OrderRepository;
import com.bootcamp.paymentproject.payment.dto.request.CreatePaymentRequest;
import com.bootcamp.paymentproject.payment.exception.PointInsufficientException;

import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.user.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import java.math.BigDecimal;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Order order;

    @BeforeEach
    void init() {
        user = User.builder()
                .username("testname")
                .email("test@test.com")
                .password("1234")
                .phone("010-1234-5678")
                .build();

        user.setId(1L);

        order = Order.create();
        order.setUser(user);
    }
    @Test
    public void 실패_포인트부족_예외발생(){

        // given
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(pointTransactionRepository.getPointSumByUserId(1L, PointType.EARN)).willReturn(BigDecimal.valueOf(0L));

        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest(1L, BigDecimal.valueOf(1000L), BigDecimal.valueOf(100L));

        // when & then
        assertThatThrownBy(() -> paymentService.createPayment(createPaymentRequest))
                .isInstanceOf(PointInsufficientException.class);
    }
}
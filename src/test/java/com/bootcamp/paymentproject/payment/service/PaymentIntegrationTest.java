package com.bootcamp.paymentproject.payment.service;

import com.bootcamp.paymentproject.membership.repository.MembershipRepository;
import com.bootcamp.paymentproject.membership.repository.UserMembershipRepository;
import com.bootcamp.paymentproject.order.entity.Order;

import com.bootcamp.paymentproject.order.entity.OrderProduct;
import com.bootcamp.paymentproject.order.repository.OrderProductRepository;
import com.bootcamp.paymentproject.order.repository.OrderRepository;
import com.bootcamp.paymentproject.payment.dto.request.CreatePaymentRequest;
import com.bootcamp.paymentproject.payment.dto.response.CreatePaymentResponse;

import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;

import com.bootcamp.paymentproject.point.repository.PointSpendHistoryRepository;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.enums.ProductStatus;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.parser.Entity;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import java.math.BigDecimal;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PaymentIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private Order order;

    @BeforeEach
    void setUp(){

        User user = new User("함형우", "123456789", "010-2074-1336", "hyu1335@naver.com");
        userRepository.save(user);

        Product product = Product.builder()
                .name("프리미엄 기계식 키보드")
                .price(BigDecimal.valueOf(1000L))
                .stock(50L)
                .description("타건감이 예술인 기계식 키보드입니다.")
                .status(ProductStatus.AVAILABLE)
                .category("전자제품")
                .build();

        productRepository.save(product);

        order = Order.create();
        order.setUser(user);

        OrderProduct orderProduct = new OrderProduct(product, 1L, order);
        order.OrderProductAdd(orderProduct);

        orderRepository.save(order);
        orderProductRepository.save(orderProduct);

        em.flush();
        em.clear();
    }

    // DataInitializer 주석처리해야 정상 통과
    @Test
    void 포인트미사용_결제생성시_order_payment_연동됨(){

        //given
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest(order.getId(),order.getTotalPrice(), BigDecimal.ZERO);

        //when
        CreatePaymentResponse createPaymentResponse = paymentService.createPayment(createPaymentRequest);

        //then
        assertThat(createPaymentResponse.getId()).isNotNull();

        em.flush();
        em.clear();

        Order reloadOrder = orderRepository.findById(createPaymentResponse.getId()).get();
        assertThat(reloadOrder.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000L));

        Payment payment = paymentRepository.findById(createPaymentResponse.getId()).get();

        assertThat(payment.getOrder().getId()).isEqualTo(reloadOrder.getId());

        assertThat(payment.getAmount()).isEqualByComparingTo(reloadOrder.getTotalPrice());

        assertThat(reloadOrder.getPointToUse()).isEqualByComparingTo(BigDecimal.ZERO);
    }

}

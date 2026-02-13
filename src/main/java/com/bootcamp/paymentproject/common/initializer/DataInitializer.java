package com.bootcamp.paymentproject.common.initializer;

import com.bootcamp.paymentproject.membership.entity.Membership;
import com.bootcamp.paymentproject.membership.entity.UserMembership;
import com.bootcamp.paymentproject.membership.enums.MembershipGrade;
import com.bootcamp.paymentproject.membership.exception.MembershipErrorCode;
import com.bootcamp.paymentproject.membership.exception.MembershipException;
import com.bootcamp.paymentproject.membership.repository.MembershipRepository;
import com.bootcamp.paymentproject.membership.repository.UserMembershipRepository;
import com.bootcamp.paymentproject.order.Repository.OrderRepository;
import com.bootcamp.paymentproject.order.dto.OrderCreateRequest;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.entity.OrderProduct;
import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.enums.ProductStatus;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import com.bootcamp.paymentproject.user.entity.User;
import com.bootcamp.paymentproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public void run(String... args) {

        // 관리자 계정 생성
        User admin = User.builder()
                .username("testname")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin"))
                .phone("010-1234-5678")
                .build();

        userRepository.save(admin);

        // 데이터가 없을 때만 실행
        if (productRepository.count() == 0) {




            // 1번 상품
            productRepository.save(Product.builder()
                    .name("프리미엄 기계식 키보드")
                    .price(BigDecimal.valueOf(1000L))
                    .stock(50L)
                    .description("타건감이 예술인 기계식 키보드입니다.")
                    .status(ProductStatus.AVAILABLE)
                    .category("전자제품")
                    .build());

            // 2번 상품
            productRepository.save(Product.builder()
                    .name("무선 인체공학 마우스")
                    .price(BigDecimal.valueOf(1000L))
                    .stock(30L)
                    .description("손목이 편안한 무선 마우스입니다.")
                    .status(ProductStatus.AVAILABLE)
                    .category("전자제품")
                    .build());

            // 3번 상품
            productRepository.save(Product.builder()
                    .name("4K 커브드 모니터")
                    .price(BigDecimal.valueOf(350000L))
                    .stock(0L)
                    .description("압도적인 몰입감의 4K 모니터입니다.")
                    .status(ProductStatus.OUT_OF_STOCK)
                    .category("전자제품")
                    .build());

            System.out.println("✅ [성공] 상품 초기 데이터 3종이 빌더 패턴으로 생성되었습니다.");
        }

        if (membershipRepository.count() == 0) {
            membershipRepository.save(
                    Membership.builder()
                            .earnRate(new BigDecimal("0.01"))
                            .gradeName(MembershipGrade.NORMAL)
                            .minTotalPaidAmount(new BigDecimal("0"))
                            .build()
            );

            membershipRepository.save(
                    Membership.builder()
                            .earnRate(new BigDecimal("0.05"))
                            .gradeName(MembershipGrade.VIP)
                            .minTotalPaidAmount(new BigDecimal("1000"))
                            .build()
            );

            membershipRepository.save(
                    Membership.builder()
                            .earnRate(new BigDecimal("0.07"))
                            .gradeName(MembershipGrade.HALF_VVIP)
                            .minTotalPaidAmount(new BigDecimal("2000"))
                            .build()
            );

            membershipRepository.save(
                    Membership.builder()
                            .earnRate(new BigDecimal("0.1"))
                            .gradeName(MembershipGrade.VVIP)
                            .minTotalPaidAmount(new BigDecimal("3000"))
                            .build()
            );

            // 관리자 멤버십 정보 추가
            Membership membership = membershipRepository.findByGradeName(MembershipGrade.NORMAL)
                    .orElseThrow(
                            () -> new MembershipException(MembershipErrorCode.NOT_FOUND_GRADE)
                    );

            UserMembership userMembership = UserMembership.builder()
                    .totalAmount(new BigDecimal("0"))
                    .user(admin)
                    .membership(membership)
                    .build();

            userMembershipRepository.save(userMembership);

        }

        // 주문 정보 생성
        Order order = Order.create();
        order.setUser(admin);

        OrderCreateRequest.Item item1 = new OrderCreateRequest.Item("1", 2L);
        OrderCreateRequest.Item item2 = new OrderCreateRequest.Item("2", 1L);

        List<OrderCreateRequest.Item> items = List.of(item1, item2);

        for (OrderCreateRequest.Item item : items) {
            Product product = productRepository.findById(Long.valueOf(item.getProductId()))
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            if (product.getStock() < item.getQuantity())
                throw new RuntimeException("재고 부족");

            OrderProduct op = new OrderProduct(product, (Long)item.getQuantity(), order);
            order.OrderProductAdd(op);
        }

        orderRepository.save(order);


        PointTransaction tx1 = new PointTransaction(
                BigDecimal.valueOf(800L),
                PointType.HOLDING,
                order
        );

        PointTransaction tx2 = new PointTransaction(
                BigDecimal.valueOf(400L),
                PointType.HOLDING,
                order
        );

        tx1.updateType(PointType.EARN);
        tx2.updateType(PointType.EARN);
        pointTransactionRepository.save(tx1);
        pointTransactionRepository.save(tx2);

    }
}
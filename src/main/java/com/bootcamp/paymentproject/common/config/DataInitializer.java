package com.bootcamp.paymentproject.common.config;

import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.enums.ProductStatus;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // 데이터가 없을 때만 실행
        if (productRepository.count() == 0) {

            // 1번 상품
            productRepository.save(Product.builder()
                    .name("프리미엄 기계식 키보드")
                    .price(BigDecimal.valueOf(120000L))
                    .stock(50L)
                    .description("타건감이 예술인 기계식 키보드입니다.")
                    .status(ProductStatus.AVAILABLE)
                    .category("전자제품")
                    .build());

            // 2번 상품
            productRepository.save(Product.builder()
                    .name("무선 인체공학 마우스")
                    .price(BigDecimal.valueOf(45000L))
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
    }
}
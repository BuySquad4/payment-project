package com.bootcamp.paymentproject.common.config;

import com.bootcamp.paymentproject.entity.Product;
import com.bootcamp.paymentproject.entity.ProductStatus;
import com.bootcamp.paymentproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            // 필드 순서: ID, 이름, 가격, 재고, 설명, 상태, 카테고리 (총 7개)
            productRepository.save(new Product(
                    "PROD-001",
                    "프리미엄 기계식 키보드",
                    120000L,
                    50,
                    "타건감이 예술인 기계식 키보드입니다.",
                    ProductStatus.AVAILABLE,
                    "전자제품"
            ));

            productRepository.save(new Product(
                    "PROD-002",
                    "무선 인체공학 마우스",
                    45000L,
                    30,
                    "손목이 편안한 무선 마우스입니다.",
                    ProductStatus.AVAILABLE,
                    "전자제품"
            ));

            productRepository.save(new Product(
                    "PROD-003",
                    "4K 커브드 모니터",
                    350000L,
                    0,
                    "압도적인 몰입감의 4K 모니터입니다.",
                    ProductStatus.OUT_OF_STOCK,
                    "전자제품"
            ));

            System.out.println("✅ [성공] 7개 필드가 포함된 상품 데이터가 생성되었습니다.");
        }
    }
}
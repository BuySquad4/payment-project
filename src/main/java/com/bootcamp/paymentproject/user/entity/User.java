    package com.bootcamp.paymentproject.user.entity;

    import com.bootcamp.paymentproject.common.entity.BaseEntity;
    import jakarta.persistence.*;
    import lombok.*;
    import org.springframework.data.annotation.CreatedDate;
    import org.springframework.data.annotation.LastModifiedDate;
    import org.springframework.data.jpa.domain.support.AuditingEntityListener;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    @Entity
    @Setter
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    @AllArgsConstructor
    @Table(name = "users")
    public class User extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true, length = 50)
        private String username;

        @Column(nullable = false)
        private String password;

        @Column(nullable = false)
        private String phone;

        @Column(nullable = false)
        private String email;

        @Column(name = "point_balance")
        private BigDecimal pointBalance;


        public void updatePointBalance(BigDecimal amount) {
            // 기존 잔액에 새로운 금액(amount)을 더함 (마이너스면 자동으로 깎임)
            this.pointBalance = this.pointBalance.add(amount);
        }
    }
    package com.bootcamp.paymentproject.user.entity;

    import com.bootcamp.paymentproject.common.entity.BaseEntity;
    import com.bootcamp.paymentproject.membership.entity.UserMembership;
    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;

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

        @OneToOne(mappedBy = "user")
        private UserMembership userMembership;

        public void updatePointBalance(BigDecimal amount) {
            // 기존 잔액에 새로운 금액(amount)을 더함 (마이너스면 자동으로 깎임)
            this.pointBalance = this.pointBalance.add(amount);
        }

        public User(String username, String password, String phone, String email) {
            this.username = username;
            this.password = password;
            this.phone = phone;
            this.email = email;
        }
    }
package com.bootcamp.paymentproject.user.dto.response;

import com.bootcamp.paymentproject.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class GetCurrentUserResponse {
    private final String customerUid;
    private final String email;
    private final String name;
    private final String phone;
    private final BigDecimal PointBalance;

    public static GetCurrentUserResponse fromEntity(User user) {
        return new GetCurrentUserResponse(
                "CUST_" + Math.abs(user.getEmail().hashCode()),
                user.getEmail(),
                user.getUsername(),
                user.getPhone(),
                BigDecimal.ZERO
        );
    }
}

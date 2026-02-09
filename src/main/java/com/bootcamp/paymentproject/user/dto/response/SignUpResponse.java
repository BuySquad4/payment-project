package com.bootcamp.paymentproject.user.dto.response;

import com.bootcamp.paymentproject.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SignUpResponse {
    private boolean success;

    private String name;

    private String phoneNumber;

    private String email;

    private LocalDateTime createdAt;

    public static SignUpResponse fromEntity(User user){
        return SignUpResponse.builder()
                .name(user.getUsername())
                .phoneNumber(user.getPhone())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

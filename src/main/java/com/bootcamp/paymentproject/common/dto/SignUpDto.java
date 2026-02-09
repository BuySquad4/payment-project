package com.bootcamp.paymentproject.common.dto;

import com.bootcamp.paymentproject.user.entity.User;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class SignUpDto {
    @Getter
    public static class Request {
        @NotBlank(message = "이름은 필수 기입란 입니다.")
        private String name;

        @NotBlank(message = "비밀번호는 공백이 될 수 없습니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        private String password;

        @NotBlank(message = "폰번호는 필수 기입란 입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "폰번호 형식이 올바르지 않습니다.")
        private String phoneNumber;

        @NotBlank(message = "이메일은 필수 기입란 입니다.")
        @Email
        private String email;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private String name;

        private String phoneNumber;

        private String email;

        private LocalDateTime createdAt;

        public static SignUpDto.Response fromEntity(User user){
            return Response.builder()
                    .name(user.getUsername())
                    .phoneNumber(user.getPhone())
                    .email(user.getEmail())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }
}
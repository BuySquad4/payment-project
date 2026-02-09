package com.bootcamp.paymentproject.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequest {
    @NotBlank(message = "이름은 필수 기입란 입니다.")
    private String name;

    @NotBlank(message = "비밀번호는 공백이 될 수 없습니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "폰번호는 필수 기입란 입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "폰번호 형식이 올바르지 않습니다.")
    private String phone;

    @NotBlank(message = "이메일은 필수 기입란 입니다.")
    @Email
    private String email;
}

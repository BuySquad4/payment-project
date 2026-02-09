package com.bootcamp.paymentproject.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse<T> {
    private boolean success;   // 성공 여부
    private String message;    // 응답 메시지
    private T data;            // 실제 데이터 (알맹이)

    // 성공 응답 정적 팩토리 메서드
    public static <T> SuccessResponse<T> success(T data, String message) {
        return SuccessResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

}
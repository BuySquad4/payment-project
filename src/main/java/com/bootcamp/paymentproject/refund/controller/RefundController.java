package com.bootcamp.paymentproject.refund.controller;

import com.bootcamp.paymentproject.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/refunds")
public class RefundController {

    private final RefundService refundService;

    /**
     * 환불 요청 API
     * - 주문 ID와 사용자 ID로 환불 처리
     * - 환불 가능 여부 확인 후 환불 진행
     */
    @PostMapping("/{orderId}")
    public ResponseEntity<Void> refund(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam String reason
    ) {
        refundService.refund(userId, orderId, reason);

        return ResponseEntity.ok().build();
    }
}

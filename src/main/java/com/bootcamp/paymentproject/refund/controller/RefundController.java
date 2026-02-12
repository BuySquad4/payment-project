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

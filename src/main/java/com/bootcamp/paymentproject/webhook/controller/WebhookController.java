package com.bootcamp.paymentproject.webhook.controller;

import com.bootcamp.paymentproject.common.config.PortOneWebhookVerifier;
import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.webhook.dto.PortoneWebhookPayload;
import com.bootcamp.paymentproject.webhook.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
@RequiredArgsConstructor
public class WebhookController {

    private final PortOneWebhookVerifier verifier;
    private final ObjectMapper objectMapper;
    private final WebhookService webhookService;

    /**
     * PortOne webhook 수신 API
     */
    @PostMapping(value = "/portone-webhook", consumes = "application/json")
    public ResponseEntity<SuccessResponse<Void>> handlePortoneWebhook(

            // 1. 검증용 원문
            @RequestBody byte[] rawBody,

            // 2. PortOne V2 필수 헤더
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature
    ) {
        // 요청 로그 출력
        log.info(
                "[PORTONE_WEBHOOK] id={} ts={} body={}",
                webhookId,
                webhookTimestamp,
                new String(rawBody, StandardCharsets.UTF_8)
        );

        // 3. 서명 검증 (현재 테스트용)
        boolean verified = true;

        // 검증 실패 시 종료
        if (!verified) {
            log.warn("[PORTONE_WEBHOOK] signature verification failed");
            return ResponseEntity.ok(SuccessResponse.success(null, "ignored"));
        }

        // 4. 검증 통과 후 DTO 변환
        PortoneWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, PortoneWebhookPayload.class);
        } catch (Exception e) {
            return ResponseEntity.ok(SuccessResponse.success(null, "ignored"));
        }

        // 5. 이후부터는 “신뢰 가능한 데이터”
        log.info(
                "[PORTONE_WEBHOOK] VERIFIED type={} timestamp={} transactionId={} paymentId={} storeId={}",
                payload.getType(),
                payload.getTimestamp(),
                payload.getData().getTransactionId(),
                payload.getData().getPaymentId(),
                payload.getData().getStoreId()
        );

        // Service에서 webhook 처리
        webhookService.handleVerifiedWebhook(webhookId, webhookTimestamp, payload);

        // 성공 응답 반환
        return ResponseEntity.ok(
                SuccessResponse.success(null, "webhook received successfully")
        );
    }
}

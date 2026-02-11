package com.bootcamp.paymentproject.webhook.controller;

import com.bootcamp.paymentproject.common.config.PortOneWebhookVerifier;
import com.bootcamp.paymentproject.common.dto.SuccessResponse;
import com.bootcamp.paymentproject.webhook.dto.PortoneWebhookPayload;
import com.bootcamp.paymentproject.webhook.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
    @PostMapping(value = "/portone-webhook")
    public ResponseEntity<SuccessResponse<Void>> handlePortoneWebhook(

            HttpServletRequest request,   // 🔥 이 줄 추가

            // 1. 검증용 원문
            @RequestBody byte[] rawBody,

            // 2. PortOne V2 필수 헤더
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature
    ) {

        // 헤더 확인용 로그도 추가
        log.info("webhook-id={}", webhookId);
        log.info("webhook-timestamp={}", webhookTimestamp);
        log.info("webhook-signature={}", webhookSignature);

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

        // TODO (Webhook 처리 - 실습 구현 포인트)
        //
        // 1) webhook-id 멱등 처리
        //    - webhook-id UNIQUE로 이벤트 기록(webhook_event 테이블)
        //    - 이미 처리된 webhook-id면 즉시 200 반환
        //
        // 2) paymentId로 PortOne 결제 조회(SSOT)
        //    - status / amount 확인
        //    - 주문 금액과 비교
        //
        // 3) 결제/주문 상태 반영(트랜잭션)
        //    - 결제 상태 전이 검증
        //      - 막아야 하는 전이 체크 (예: REFUNDED → PAID : 이미 환불된 결제)
        //    - 재고 차감 후 확정
        //    - 성공 시 결제=결제완료, 주문=주문완료
        //
        // 4) 처리 완료 마킹
        //    - webhook_event 테이블의 처리 완료 시각 업데이트

        // Service에서 webhook 처리
        webhookService.handleVerifiedWebhook(webhookId, webhookTimestamp, payload);

        // 성공 응답 반환
        return ResponseEntity.ok(
                SuccessResponse.success(null, "webhook received successfully")
        );
    }
}

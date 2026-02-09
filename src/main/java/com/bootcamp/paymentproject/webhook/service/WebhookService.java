package com.bootcamp.paymentproject.webhook.service;

import com.bootcamp.paymentproject.webhook.dto.PortoneWebhookPayload;
import com.bootcamp.paymentproject.webhook.entity.WebhookEvent;
import com.bootcamp.paymentproject.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    // webhook_event 테이블 접근
    private final WebhookEventRepository webhookEventRepository;

    /**
     * PortOne에서 webhook이 오면 실행되는 메서드
     * - 같은 webhookId는 한 번만 처리 (중복 방지)
     * - 처리 성공/실패 상태를 DB에 저장
     */
    @Transactional
    public void handleVerifiedWebhook(String webhookId,                    // 중복인지 확인
                                      String webhookTimestamp,             // 유효한 요청인지 확인
                                      PortoneWebhookPayload payload) {     // 실제 결제 처리

        // 이미 처리된 webhook이면 아무것도 하지 않고 종료
        if (webhookEventRepository.findByWebhookId(webhookId).isPresent()) {
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

        // webhook 이벤트를 DB에 저장 (처리 시작 기록)
        WebhookEvent event;
        try {
            event = WebhookEvent.received(
                    webhookId,
                    payload.getData().getPaymentId(),
                    payload.getType(),
                    webhookTimestamp
            );
            webhookEventRepository.save(event);
        } catch (DataIntegrityViolationException e) {
            // 같은 webhookId가 이미 DB에 있으면 중복 요청이므로 무시
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

        try {
            // TODO: 결제 조회 후 Payment / Order 상태 업데이트

            // 처리 성공 → 상태를 PROCESSED로 변경
            event.markProcessed();

        } catch (Exception ex) {

            // 처리 실패 → 상태를 FAILED로 변경
            log.error("[PORTONE_WEBHOOK] processing failed webhookId={}", webhookId, ex);
            event.markFailed();
        }
    }
}

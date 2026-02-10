package com.bootcamp.paymentproject.webhook.service;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import com.bootcamp.paymentproject.order.Repository.OrderRepository;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.webhook.client.PortOneClient;
import com.bootcamp.paymentproject.webhook.dto.PortonePaymentResponse;
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

    private final WebhookEventRepository webhookEventRepository;
    private final PortOneClient portOneClient;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * PortOne webhook 처리 흐름
     * 1) webhookId 멱등 처리
     * 2) webhook_event 저장(RECEIVED)
     * 3) PortOne 결제 조회(SSOT)
     * 4) 결제/주문 상태 반영
     * 5) webhook_event 처리 결과 기록(PROCESSED/FAILED)
     */
    public void handleVerifiedWebhook(String webhookId,                    // 중복인지 확인
                                      String webhookTimestamp,             // 유효한 요청인지 확인
                                      PortoneWebhookPayload payload) {     // 실제 결제 처리

        // TODO 2) PortOne 결제 조회(SSOT)
        String paymentId = payload.getData().getPaymentId();

        PortonePaymentResponse result = portOneClient.getPayment(paymentId);
        if (result == null) {
            throw new ServiceException(ErrorCode.PORTONE_RESPONSE_NULL);
        }

        log.info("[PORTONE_PAYMENT] paymentId={}, status={}, amount={}",
                result.getPaymentId(), result.getStatus(), result.getAmount());

        // 외부 API 조회가 끝난 다음에, DB 반영 로직만 트랜잭션으로 처리
        handleAfterFetch(webhookId, webhookTimestamp, payload, result);
    }

    @Transactional
    public void handleAfterFetch(String webhookId,                    // 중복인지 확인
                                      String webhookTimestamp,             // 유효한 요청인지 확인
                                      PortoneWebhookPayload payload,       // 실제 결제 처리
                                      PortonePaymentResponse result) {

        // TODO 1) 멱등: 이미 처리된 webhookId면 종료
        if (webhookEventRepository.findByWebhookId(webhookId).isPresent()) {
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

        // 수신 이벤트 저장(RECEIVED)
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
            // UNIQUE 제약으로 중복이면 무시
            log.info("[PORTONE_WEBHOOK] duplicate webhookId={}, ignore", webhookId);
            return;
        }

        try {
            // TODO 3) 결제/주문 상태 반영(Payment/Order 업데이트)

            // TODO 4) 처리 완료 마킹
            // 5) 처리 성공 기록(PROCESSED)
            event.markProcessed();

        } catch (ServiceException ex) {
            log.error("[PORTONE_WEBHOOK] processing failed webhookId={}, code={}",
                    webhookId, ex.getErrorCode().getCode(), ex);
            event.markFailed();

        } catch (Exception ex) {
            log.error("[PORTONE_WEBHOOK] unexpected error webhookId={}", webhookId, ex);
            event.markFailed();
        }
    }
}

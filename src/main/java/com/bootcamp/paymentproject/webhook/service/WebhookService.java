package com.bootcamp.paymentproject.webhook.service;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.portone.client.PortOneClient;
import com.bootcamp.paymentproject.webhook.dto.PortoneWebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final PortOneClient portOneClient;
    private final WebhookTxService webhookTxService;

    /**
     * PortOne webhook 처리
     *
     * 흐름:
     * 1) webhook payload에서 paymentId 추출
     * 2) PortOne API로 실제 결제 정보 조회
     * 3) 조회 결과를 기반으로 DB 상태 변경 (트랜잭션 처리)
     */
    public void handleVerifiedWebhook(String webhookId,
                                      String webhookTimestamp,
                                      PortoneWebhookPayload payload) {

        // paymentId 추출
        String paymentId = payload.getData().getPaymentId();

        // PortOne API로 결제 상태 조회
        PortOnePaymentResponse result = portOneClient.getPayment(paymentId);
        if (result == null) {
            throw new ServiceException(ErrorCode.PORTONE_RESPONSE_NULL);
        }

        // 조회 결과 로그
        log.info("[PORTONE_PAYMENT] paymentId={}, status={}, amount={}",
                result.getPaymentId(), result.getStatus(), result.getAmount());

        // DB 상태 반영 (결제 상태 변경, webhook_event 기록)
        webhookTxService.handleAfterFetch(webhookId, webhookTimestamp, payload, result);
    }
}

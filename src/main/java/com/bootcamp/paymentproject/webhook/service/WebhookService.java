package com.bootcamp.paymentproject.webhook.service;

import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import com.bootcamp.paymentproject.order.entity.Order;
import com.bootcamp.paymentproject.order.entity.OrderProduct;
import com.bootcamp.paymentproject.payment.entity.Payment;
import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import com.bootcamp.paymentproject.payment.repository.PaymentRepository;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import com.bootcamp.paymentproject.portone.client.PortOneClient;
import com.bootcamp.paymentproject.product.entity.Product;
import com.bootcamp.paymentproject.product.repository.ProductRepository;
import com.bootcamp.paymentproject.webhook.dto.PortoneWebhookPayload;
import com.bootcamp.paymentproject.webhook.entity.WebhookEvent;
import com.bootcamp.paymentproject.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final PortOneClient portOneClient;
    private final WebhookTxService webhookTxService;

    /**
     * PortOne webhook 처리 흐름
     //* 1) webhookId 멱등 처리(중복이면 무시)
     * 2) webhook_event 저장(RECEIVED)
     //* 3) PortOne 결제 조회(SSOT)
     //* 4) 결제/주문 상태 DB 반영
     //* 5) webhook_event 처리 결과 기록(PROCESSED/FAILED)
     */
    public void handleVerifiedWebhook(String webhookId,                    // 중복인지 확인
                                      String webhookTimestamp,             // 유효한 요청인지 확인
                                      PortoneWebhookPayload payload) {     // 실제 결제 처리

        // TODO 2) PortOne 결제 조회(SSOT)
        String paymentId = payload.getData().getPaymentId();

        PortOnePaymentResponse result = portOneClient.getPayment(paymentId);
        if (result == null) {
            throw new ServiceException(ErrorCode.PORTONE_RESPONSE_NULL);
        }

        log.info("[PORTONE_PAYMENT] paymentId={}, status={}, amount={}",
                result.getPaymentId(), result.getStatus(), result.getAmount());

        // 외부 API 조회가 끝난 다음에, DB 반영 로직만 트랜잭션으로 처리
        webhookTxService.handleAfterFetch(webhookId, webhookTimestamp, payload, result);
    }
}

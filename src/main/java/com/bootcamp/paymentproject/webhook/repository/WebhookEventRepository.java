package com.bootcamp.paymentproject.webhook.repository;

import com.bootcamp.paymentproject.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    /**
     * webhookId로 이벤트 조회
     * - 이미 처리된 webhook인지 확인할 때 사용
     */
    Optional<WebhookEvent> findByWebhookId(String webhookId);

    /**
     * webhookId가 이미 존재하는지 확인
     * - 중복 webhook 요청 방지 (멱등 처리)
     */
    boolean existsByWebhookId(String webhookId);
}

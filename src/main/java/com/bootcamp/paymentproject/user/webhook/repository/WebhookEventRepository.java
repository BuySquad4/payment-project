package com.bootcamp.paymentproject.user.webhook.repository;

import com.bootcamp.paymentproject.user.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Optional<WebhookEvent> findByWebhookId(String webhookId);
}

package com.bootcamp.paymentproject.webhook.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.webhook.enums.WebhookEventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "webhook_event",
        // webhookId 중복 저장 방지 (멱등 처리용)
        uniqueConstraints = @UniqueConstraint(name = "uk_webhook_event_webhook_id", columnNames = "webhookId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String webhookId;
    @Column(nullable = false)
    private String paymentID;
    @Column(nullable = false)
    private String eventStatus;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private WebhookEventStatus status;
    @Column(nullable = false)
    private LocalDateTime receivedAt;
    private LocalDateTime completedAt;

    public static WebhookEvent received(String webhookId, String paymentId, String eventStatus, String rawTs) {
        WebhookEvent e = new WebhookEvent();
        e.webhookId = webhookId;
        e.paymentID = paymentId;
        e.eventStatus = eventStatus;
        e.status = WebhookEventStatus.RECEIVED;
        e.receivedAt = LocalDateTime.now();
        e.completedAt = null;
        return e;
    }

    public void markProcessed() {
        this.status = WebhookEventStatus.PROCESSED;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = WebhookEventStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }
}

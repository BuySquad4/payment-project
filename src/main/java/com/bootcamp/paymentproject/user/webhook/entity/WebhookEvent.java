package com.bootcamp.paymentproject.user.webhook.entity;

import com.bootcamp.paymentproject.common.entity.BaseEntity;
import com.bootcamp.paymentproject.user.webhook.enums.WebhookEventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "webhook_event")
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
    @Column(nullable = false)
    private LocalDateTime completedAt;
}

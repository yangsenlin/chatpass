package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MessageQueue 实体
 * 消息队列配置
 */
@Entity
@Table(name = "message_queues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "realm_id", nullable = false)
    private Long realmId;

    @Column(name = "queue_name", nullable = false, unique = true, length = 60)
    private String queueName;

    @Column(name = "queue_type", nullable = false, length = 20)
    private String queueType;

    @Column(name = "broker_url", nullable = false, length = 200)
    private String brokerUrl;

    @Column(name = "exchange_name", length = 60)
    private String exchangeName;

    @Column(name = "routing_key", length = 60)
    private String routingKey;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "max_retry", nullable = false)
    @Builder.Default
    private Integer maxRetry = 3;

    @Column(name = "retry_delay_seconds")
    @Builder.Default
    private Integer retryDelaySeconds = 30;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Queue types
    public static final String TYPE_RABBITMQ = "RABBITMQ";
    public static final String TYPE_KAFKA = "KAFKA";
    public static final String TYPE_AMAZON_SQS = "SQS";
}
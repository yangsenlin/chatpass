package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Webhook 实体 - 外部系统集成
 * 对应 Zulip Webhook 系统
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "webhooks", indexes = {
    @Index(name = "idx_webhook_owner", columnList = "owner_id"),
    @Index(name = "idx_webhook_realm", columnList = "realm_id"),
    @Index(name = "idx_webhook_key", columnList = "webhook_key")
})
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Webhook 名称
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Webhook Key（用于验证）
    @Column(name = "webhook_key", nullable = false, unique = true, length = 64)
    private String webhookKey;

    // Webhook URL（接收端点）
    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    // 所有者
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // Realm
    @Column(name = "realm_id", nullable = false)
    private Long realmId;

    // 关联的 Bot（可选）
    @Column(name = "bot_id")
    private Long botId;

    // 目标 Stream
    @Column(name = "target_stream_id")
    private Long targetStreamId;

    // 默认 Topic
    @Column(name = "default_topic", length = 60)
    private String defaultTopic;

    // 事件类型列表（JSON）
    @Column(name = "event_types", columnDefinition = "TEXT")
    private String eventTypes;

    // 描述
    @Column(name = "description", length = 500)
    private String description;

    // 是否激活
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 请求方法: POST, PUT
    @Column(name = "request_method", length = 10)
    @Builder.Default
    private String requestMethod = "POST";

    // 请求头（JSON）
    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    // 请求体模板（JSON）
    @Column(name = "request_body_template", columnDefinition = "TEXT")
    private String requestBodyTemplate;

    // 重试次数
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 3;

    // 重试间隔（秒）
    @Column(name = "retry_interval")
    @Builder.Default
    private Integer retryInterval = 5;

    // 最后调用时间
    @Column(name = "last_invoked")
    private LocalDateTime lastInvoked;

    // 最后调用结果
    @Column(name = "last_result", length = 20)
    private String lastResult;

    // 调用次数统计
    @Column(name = "invoke_count")
    @Builder.Default
    private Long invokeCount = 0L;

    // 成功次数
    @Column(name = "success_count")
    @Builder.Default
    private Long successCount = 0L;

    // 失败次数
    @Column(name = "failure_count")
    @Builder.Default
    private Long failureCount = 0L;

    // 创建时间
    @CreationTimestamp
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /**
     * 生成 Webhook Key
     */
    public static String generateWebhookKey() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 计算成功率
     */
    public double getSuccessRate() {
        if (invokeCount == 0) return 0;
        return (double) successCount / invokeCount * 100;
    }
}
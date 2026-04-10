package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WebhookLog 实体 - Webhook 调用日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "webhook_logs", indexes = {
    @Index(name = "idx_log_webhook", columnList = "webhook_id"),
    @Index(name = "idx_log_time", columnList = "invoke_time"),
    @Index(name = "idx_log_result", columnList = "result")
})
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Webhook
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", nullable = false)
    private Webhook webhook;

    // 事件类型
    @Column(name = "event_type", length = 50)
    private String eventType;

    // 事件数据（JSON）
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;

    // 请求 URL
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    // 请求方法
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    // 请求头（JSON）
    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    // 请求体（JSON）
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    // 响应状态码
    @Column(name = "response_status")
    private Integer responseStatus;

    // 响应体
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    // 调用结果: SUCCESS, FAILURE, TIMEOUT
    @Column(name = "result", nullable = false, length = 20)
    private String result;

    // 错误信息
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // 重试次数
    @Column(name = "retry_attempt")
    @Builder.Default
    private Integer retryAttempt = 0;

    // 调用时间
    @CreationTimestamp
    @Column(name = "invoke_time", nullable = false)
    private LocalDateTime invokeTime;

    // 响应时间（毫秒）
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    // 结果常量
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAILURE = "FAILURE";
    public static final String RESULT_TIMEOUT = "TIMEOUT";
}
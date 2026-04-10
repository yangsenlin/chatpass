package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * QueuedMessage 实体
 * 队列消息记录
 */
@Entity
@Table(name = "queued_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueuedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_id", nullable = false)
    private Long queueId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "date_queued", nullable = false)
    private LocalDateTime dateQueued;

    @Column(name = "date_sent")
    private LocalDateTime dateSent;

    @Column(name = "last_retry")
    private LocalDateTime lastRetry;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_RETRYING = "RETRYING";
}
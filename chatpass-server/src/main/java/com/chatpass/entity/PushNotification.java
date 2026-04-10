package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PushNotification 实体
 * 推送通知记录
 */
@Entity
@Table(name = "push_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "push_config_id", nullable = false)
    private Long pushConfigId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "device_token", nullable = false, length = 200)
    private String deviceToken;

    @Column(name = "notification_type", nullable = false, length = 20)
    private String notificationType;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "data_payload", columnDefinition = "TEXT")
    private String dataPayload;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "date_sent")
    private LocalDateTime dateSent;

    // Notification types
    public static final String TYPE_MESSAGE = "MESSAGE";
    public static final String TYPE_MENTION = "MENTION";
    public static final String TYPE_STREAM = "STREAM";
    public static final String TYPE_PRIVATE = "PRIVATE";
    
    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";
}
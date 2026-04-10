package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AuditLog 实体 - 审计日志
 * 记录系统中的所有重要操作
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_time", columnList = "event_time"),
    @Index(name = "idx_audit_type", columnList = "event_type"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 操作用户
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    // 事件类型: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    // 事件描述
    @Column(name = "event_description", length = 500)
    private String eventDescription;

    // 资源类型: MESSAGE, STREAM, USER, etc.
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    // 资源 ID
    @Column(name = "resource_id")
    private Long resourceId;

    // 操作前数据（JSON）
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    // 操作后数据（JSON）
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    // 操作结果: SUCCESS, FAILURE
    @Column(name = "result", nullable = false, length = 20)
    @Builder.Default
    private String result = "SUCCESS";

    // 错误信息（失败时）
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    // IP 地址
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    // 用户代理
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // Realm ID
    @Column(name = "realm_id")
    private Long realmId;

    // 事件时间
    @CreationTimestamp
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    // 请求 ID（追踪链）
    @Column(name = "request_id", length = 100)
    private String requestId;

    // 事件类型常量
    public static final String TYPE_CREATE = "CREATE";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_LOGOUT = "LOGOUT";
    public static final String TYPE_READ = "READ";
    public static final String TYPE_SEND = "SEND";
    public static final String TYPE_SUBSCRIBE = "SUBSCRIBE";
    public static final String TYPE_UNSUBSCRIBE = "UNSUBSCRIBE";
    public static final String TYPE_INVITE = "INVITE";
    public static final String TYPE_JOIN = "JOIN";
    public static final String TYPE_LEAVE = "LEAVE";

    // 资源类型常量
    public static final String RESOURCE_MESSAGE = "MESSAGE";
    public static final String RESOURCE_STREAM = "STREAM";
    public static final String RESOURCE_USER = "USER";
    public static final String RESOURCE_USER_GROUP = "USER_GROUP";
    public static final String RESOURCE_INVITE = "INVITE";
    public static final String RESOURCE_REACTION = "REACTION";
    public static final String RESOURCE_UPLOAD = "UPLOAD";
    public static final String RESOURCE_EMOJI = "EMOJI";
    public static final String RESOURCE_STAR = "STAR";
    public static final String RESOURCE_BOT = "BOT";
    public static final String RESOURCE_WEBHOOK = "WEBHOOK";

    // 结果常量
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAILURE = "FAILURE";
}
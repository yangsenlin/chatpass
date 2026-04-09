package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Realm 实体 - Zulip 组织/域
 * 对应 Zulip Realm model
 * 
 * 一个 Realm 代表一个独立的 Zulip 组织
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "realms")
public class Realm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "string_id", unique = true, nullable = false)
    private String stringId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "invite_required")
    @Builder.Default
    private Boolean inviteRequired = true;

    @Column(name = "create_stream_policy")
    @Builder.Default
    private Integer createStreamPolicy = 1;

    @Column(name = "invite_to_stream_policy")
    @Builder.Default
    private Integer inviteToStreamPolicy = 1;

    @Column(name = "default_language")
    @Builder.Default
    private String defaultLanguage = "en";

    @Column(name = "default_twenty_four_hour_time")
    @Builder.Default
    private Boolean defaultTwentyFourHourTime = false;

    // 消息内容设置
    @Column(name = "message_content_allowed_in_email_notifications")
    @Builder.Default
    private Boolean messageContentAllowedInEmailNotifications = true;

    // 消息编辑设置
    @Column(name = "allow_message_editing")
    @Builder.Default
    private Boolean allowMessageEditing = true;

    @Column(name = "message_edit_time_limit_seconds")
    @Builder.Default
    private Integer messageEditTimeLimitSeconds = 600;

    // 消息保留策略
    @Column(name = "message_retention_days")
    private Integer messageRetentionDays;

    // 认证设置
    @Column(name = "email_auth_enabled")
    @Builder.Default
    private Boolean emailAuthEnabled = true;

    @Column(name = "password_auth_enabled")
    @Builder.Default
    private Boolean passwordAuthEnabled = true;

    // 计划类型
    @Column(name = "plan_type")
    @Builder.Default
    private Integer planType = 1; // 1=Self-hosted

    // 最大用户数
    @Column(name = "max_users")
    private Integer maxUsers;

    // 活跃状态
    @Column(name = "deactivated")
    @Builder.Default
    private Boolean deactivated = false;

    // 域名配置
    @Column(name = "domain")
    private String domain;

    // 自定义设置
    @Column(name = "custom_profile_fields", columnDefinition = "jsonb")
    @Builder.Default
    private String customProfileFields = "[]";

    @Column(name = "default_user_settings", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> defaultUserSettings = new HashMap<>();

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
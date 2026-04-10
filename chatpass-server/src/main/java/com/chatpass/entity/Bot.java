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
 * Bot 实体 - 机器人用户
 * 对应 Zulip Bot 系统
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bots", indexes = {
    @Index(name = "idx_bot_owner", columnList = "owner_id"),
    @Index(name = "idx_bot_realm", columnList = "realm_id"),
    @Index(name = "idx_bot_api_key", columnList = "api_key")
})
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bot 名称
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Bot 类型: OUTGOING (发送消息), INCOMING (接收消息), GENERIC (通用)
    @Column(name = "bot_type", nullable = false, length = 20)
    @Builder.Default
    private String botType = "OUTGOING";

    // Bot 用户 ID（关联 UserProfile）
    @Column(name = "bot_user_id", nullable = false, unique = true)
    private Long botUserId;

    // API Key（用于调用 API）
    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    // 所有者
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // Realm
    @Column(name = "realm_id", nullable = false)
    private Long realmId;

    // Avatar URL
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // Bot 配置（JSON）
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    // 前端服务 URL（Outgoing Bot）
    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    // 描述
    @Column(name = "description", length = 500)
    private String description;

    // 是否激活
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 创建时间
    @CreationTimestamp
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Bot 类型常量
    public static final String TYPE_OUTGOING = "OUTGOING";      // 发送消息到外部服务
    public static final String TYPE_INCOMING = "INCOMING";      // 从外部接收消息
    public static final String TYPE_GENERIC = "GENERIC";        // 通用 Bot

    /**
     * 生成 API Key
     */
    public static String generateApiKey() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 是否为 Outgoing Bot
     */
    public boolean isOutgoing() {
        return TYPE_OUTGOING.equals(botType);
    }

    /**
     * 是否为 Incoming Bot
     */
    public boolean isIncoming() {
        return TYPE_INCOMING.equals(botType);
    }
}
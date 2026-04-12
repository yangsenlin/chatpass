package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 机器人实体
 * 用于管理Bot用户和API Key
 */
@Entity
@Table(name = "bots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Bot用户ID（关联UserProfile）
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * Bot名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Bot类型
     */
    @Column(name = "bot_type", length = 20)
    @Builder.Default
    private String botType = "generic"; // generic, outgoing_webhook, incoming_webhook
    
    /**
     * API Key
     */
    @Column(name = "api_key", nullable = false, unique = true, length = 100)
    private String apiKey;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id", nullable = false)
    private Long realmId;
    
    /**
     * 创建者ID（Bot的所有者）
     */
    @Column(name = "owner_id")
    private Long ownerId;
    
    /**
     * Bot头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    /**
     * Bot描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * Webhook URL（incoming webhook）
     */
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;
    
    /**
     * 是否激活
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 话题订阅实体
 * 用于管理用户对特定话题的订阅
 */
@Entity
@Table(name = "topic_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * Stream ID
     */
    @Column(name = "stream_id", nullable = false)
    private Long streamId;
    
    /**
     * 话题名称
     */
    @Column(name = "topic", nullable = false, length = 100)
    private String topic;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    /**
     * 是否静音
     */
    @Column(name = "is_muted")
    @Builder.Default
    private Boolean isMuted = false;
    
    /**
     * 通知设置
     */
    @Column(name = "notification_settings", length = 20)
    @Builder.Default
    private String notificationSettings = "all"; // all, mentions, none
    
    /**
     * 订阅时间
     */
    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        subscribedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

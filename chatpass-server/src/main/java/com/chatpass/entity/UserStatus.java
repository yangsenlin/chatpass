package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 用户状态消息实体
 * 用于管理用户的状态消息和心情
 */
@Entity
@Table(name = "user_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 状态文本
     */
    @Column(name = "status_text", length = 200)
    private String statusText;
    
    /**
     * 状态Emoji
     */
    @Column(name = "status_emoji", length = 50)
    private String statusEmoji;
    
    /**
     * 状态有效期（秒）
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    /**
     * 状态过期时间
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
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

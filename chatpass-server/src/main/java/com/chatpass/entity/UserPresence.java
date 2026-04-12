package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 用户在线状态实体
 * 用于管理用户的在线/离线状态
 */
@Entity
@Table(name = "user_presences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 在线状态
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "offline"; // online, offline, idle, busy
    
    /**
     * 状态消息
     */
    @Column(name = "status_message", length = 200)
    private String statusMessage;
    
    /**
     * 最后活跃时间
     */
    @Column(name = "last_active")
    private LocalDateTime lastActive;
    
    /**
     * 最后离线时间
     */
    @Column(name = "last_offline")
    private LocalDateTime lastOffline;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 是否推送通知开启
     */
    @Column(name = "push_notifications")
    @Builder.Default
    private Boolean pushNotifications = true;
    
    /**
     * 是否显示离线状态
     */
    @Column(name = "show_offline")
    @Builder.Default
    private Boolean showOffline = false;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

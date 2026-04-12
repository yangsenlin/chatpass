package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 用户静音实体
 * 用于管理用户对其他用户的静音设置
 */
@Entity
@Table(name = "user_mutes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID（静音发起者）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 被静音的用户ID
     */
    @Column(name = "muted_user_id", nullable = false)
    private Long mutedUserId;
    
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

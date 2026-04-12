package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 消息阅读状态实体
 * 用于管理用户对消息的阅读状态
 */
@Entity
@Table(name = "message_read_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReadStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 消息ID
     */
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    
    /**
     * 阅读时间
     */
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    @PrePersist
    protected void onCreate() {
        readAt = LocalDateTime.now();
    }
}

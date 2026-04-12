package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 消息草稿实体
 * 用于保存用户未发送的消息草稿
 */
@Entity
@Table(name = "message_drafts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDraft {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 所属用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 目标Stream ID（如果是Stream消息）
     */
    @Column(name = "stream_id")
    private Long streamId;
    
    /**
     * 目标用户ID列表（如果是私信，JSON格式）
     */
    @Column(name = "to_user_ids", length = 500)
    private String toUserIds;
    
    /**
     * 话题（Stream消息）
     */
    @Column(name = "topic", length = 100)
    private String topic;
    
    /**
     * 草稿内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    /**
     * 消息类型
     */
    @Column(name = "type", length = 20)
    private String type; // stream, private
    
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

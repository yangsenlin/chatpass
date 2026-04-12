package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 消息归档实体
 * 用于管理归档的消息记录
 */
@Entity
@Table(name = "message_archives")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageArchive {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 原消息ID
     */
    @Column(name = "original_message_id")
    private Long originalMessageId;
    
    /**
     * 消息内容（归档副本）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    /**
     * 渲染后的内容
     */
    @Column(name = "rendered_content", columnDefinition = "TEXT")
    private String renderedContent;
    
    /**
     * 发送者ID
     */
    @Column(name = "sender_id")
    private Long senderId;
    
    /**
     * Stream ID
     */
    @Column(name = "stream_id")
    private Long streamId;
    
    /**
     * 话题
     */
    @Column(name = "topic", length = 100)
    private String topic;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id", nullable = false)
    private Long realmId;
    
    /**
     * 原发送时间
     */
    @Column(name = "original_date_sent")
    private LocalDateTime originalDateSent;
    
    /**
     * 归档时间
     */
    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;
    
    /**
     * 归档策略
     */
    @Column(name = "archive_policy", length = 50)
    private String archivePolicy; // retention, manual, size_limit
    
    /**
     * 归档者ID
     */
    @Column(name = "archived_by")
    private Long archivedBy;
    
    /**
     * 是否可恢复
     */
    @Column(name = "is_recoverable")
    @Builder.Default
    private Boolean isRecoverable = true;
    
    /**
     * 恢复截止时间
     */
    @Column(name = "recover_until")
    private LocalDateTime recoverUntil;
    
    @PrePersist
    protected void onCreate() {
        archivedAt = LocalDateTime.now();
    }
}

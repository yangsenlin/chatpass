package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 固定消息实体
 * 用于管理频道/话题的固定消息
 */
@Entity
@Table(name = "pinned_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinnedMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 消息ID
     */
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    
    /**
     * Stream ID（固定到频道）
     */
    @Column(name = "stream_id")
    private Long streamId;
    
    /**
     * 话题（固定到话题）
     */
    @Column(name = "topic", length = 100)
    private String topic;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id", nullable = false)
    private Long realmId;
    
    /**
     * 固定者ID
     */
    @Column(name = "pinned_by")
    private Long pinnedBy;
    
    /**
     * 固定时间
     */
    @Column(name = "pinned_at", nullable = false)
    private LocalDateTime pinnedAt;
    
    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    /**
     * 是否过期
     */
    @Column(name = "is_expired")
    @Builder.Default
    private Boolean isExpired = false;
    
    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        pinnedAt = LocalDateTime.now();
    }
}

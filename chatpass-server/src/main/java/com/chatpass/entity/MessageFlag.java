package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 消息标记实体
 * 用于管理用户对消息的标记/收藏
 */
@Entity
@Table(name = "message_flags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageFlag {
    
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
     * 标记时间
     */
    @Column(name = "flagged_at", nullable = false)
    private LocalDateTime flaggedAt;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    /**
     * 标记类型
     */
    @Column(name = "flag_type", length = 20)
    @Builder.Default
    private String flagType = "star"; // star, bookmark, important
    
    @PrePersist
    protected void onCreate() {
        flaggedAt = LocalDateTime.now();
    }
}

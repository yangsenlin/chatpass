package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 用户反应实体
 * 用于管理用户对消息的表情反应
 */
@Entity
@Table(name = "user_reactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 消息ID
     */
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 反应类型（emoji）
     */
    @Column(name = "reaction_type", nullable = false, length = 50)
    private String reactionType; // emoji code or name
    
    /**
     * 反应时间
     */
    @Column(name = "reacted_at", nullable = false)
    private LocalDateTime reactedAt;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    @PrePersist
    protected void onCreate() {
        reactedAt = LocalDateTime.now();
    }
}

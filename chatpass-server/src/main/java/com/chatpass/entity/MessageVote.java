package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 消息投票实体
 * 用于管理消息的投票/评分
 */
@Entity
@Table(name = "message_votes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 消息ID
     */
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    
    /**
     * 投票用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 投票类型
     */
    @Column(name = "vote_type", length = 20)
    @Builder.Default
    private String voteType = "upvote"; // upvote, downvote
    
    /**
     * 投票时间
     */
    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    @PrePersist
    protected void onCreate() {
        votedAt = LocalDateTime.now();
    }
}

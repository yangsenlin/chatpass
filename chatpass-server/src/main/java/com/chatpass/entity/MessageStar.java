package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * MessageStar 实体 - 消息收藏
 * 对应 Zulip UserMessage flag system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_stars", indexes = {
    @Index(name = "idx_stars_message", columnList = "message_id"),
    @Index(name = "idx_stars_user", columnList = "user_id"),
    @Index(name = "idx_stars_time", columnList = "starred_time")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_message_user", columnNames = {"message_id", "user_id"})
})
public class MessageStar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 消息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // 用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    // 收藏时间
    @CreationTimestamp
    @Column(name = "starred_time", nullable = false)
    private LocalDateTime starredTime;

    // 备注（可选）
    @Column(name = "note", length = 500)
    private String note;

    /**
     * 是否已收藏
     */
    public boolean isStarred() {
        return starredTime != null;
    }
}
package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * UserMessage 实体 - Zulip 用户-消息关系
 * 对应 Zulip UserMessage model
 * 
 * 记录用户对消息的状态（已读、已标记等）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_messages", indexes = {
    @Index(name = "idx_user_messages_user", columnList = "user_profile_id"),
    @Index(name = "idx_user_messages_message", columnList = "message_id")
})
public class UserMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // 消息标志
    @Column(name = "flags")
    private Long flags = 0L;

    // 标志常量
    public static final long FLAG_READ = 1L;
    public static final long FLAG_STARRED = 2L;
    public static final long FLAG_COLLAPSED = 4L;
    public static final long FLAG_MENTIONED = 8L;
    public static final long FLAG_WILDCARD_MENTIONED = 16L;
    public static final long FLAG_SUMMARY = 32L;
    public static final long FLAG_HAS_ALERT_WORD = 64L;
    public static final long FLAG_HISTORICAL = 128L;

    public boolean isRead() {
        return (flags & FLAG_READ) != 0;
    }

    public void setRead(boolean read) {
        if (read) {
            flags |= FLAG_READ;
        } else {
            flags &= ~FLAG_READ;
        }
    }

    public boolean isStarred() {
        return (flags & FLAG_STARRED) != 0;
    }

    public void setStarred(boolean starred) {
        if (starred) {
            flags |= FLAG_STARRED;
        } else {
            flags &= ~FLAG_STARRED;
        }
    }

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;
}
package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * TypingStatus 实体 - 输入提示状态
 * 对应 Zulip typing events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "typing_status", indexes = {
    @Index(name = "idx_typing_user", columnList = "user_id"),
    @Index(name = "idx_typing_recipient", columnList = "recipient_id"),
    @Index(name = "idx_typing_time", columnList = "last_update")
})
public class TypingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 输入者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    // 接收者（Recipient - 用于私信）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    // Stream ID（用于频道消息）
    @Column(name = "stream_id")
    private Long streamId;

    // Topic（用于频道消息）
    @Column(name = "topic", length = 60)
    private String topic;

    // 输入类型: direct=私信, stream=频道
    @Column(name = "typing_type", nullable = false)
    @Builder.Default
    private String typingType = "direct";

    // 最后更新时间（用于判断是否仍在输入）
    @CreationTimestamp
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    // 输入状态过期时间（秒）
    @Column(name = "expires_in", nullable = false)
    @Builder.Default
    private Integer expiresInSeconds = 30;

    // 输入类型常量
    public static final String TYPE_DIRECT = "direct";
    public static final String TYPE_STREAM = "stream";

    /**
     * 判断是否仍在输入（未过期）
     */
    public boolean isStillTyping() {
        LocalDateTime expirationTime = lastUpdate.plusSeconds(expiresInSeconds);
        return LocalDateTime.now().isBefore(expirationTime);
    }

    /**
     * 获取剩余有效时间（秒）
     */
    public int getRemainingSeconds() {
        LocalDateTime expirationTime = lastUpdate.plusSeconds(expiresInSeconds);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expirationTime)) {
            return 0;
        }
        return (int) java.time.Duration.between(now, expirationTime).getSeconds();
    }
}
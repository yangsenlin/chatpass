package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * TopicSubscription 实体 - 话题订阅
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "topic_subscriptions", indexes = {
    @Index(name = "idx_topic_sub_user", columnList = "user_id"),
    @Index(name = "idx_topic_sub_stream", columnList = "stream_id"),
    @Index(name = "idx_topic_sub_topic", columnList = "topic_name")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_topic_subscription", columnNames = {"user_id", "stream_id", "topic_name"})
})
public class TopicSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Stream
    @Column(name = "stream_id", nullable = false)
    private Long streamId;

    // Topic 名称
    @Column(name = "topic_name", nullable = false, length = 60)
    private String topicName;

    // 订阅类型: NOTIFY（通知）、MENTION_ONLY（仅提及）、MUTE（静音）
    @Column(name = "subscription_type", length = 20)
    @Builder.Default
    private String subscriptionType = "NOTIFY";

    // 是否启用桌面通知
    @Column(name = "desktop_notifications")
    @Builder.Default
    private Boolean desktopNotifications = true;

    // 是否启用邮件通知
    @Column(name = "email_notifications")
    @Builder.Default
    private Boolean emailNotifications = false;

    // 是否启用推送通知
    @Column(name = "push_notifications")
    @Builder.Default
    private Boolean pushNotifications = true;

    // 是否启用声音通知
    @Column(name = "sound_notifications")
    @Builder.Default
    private Boolean soundNotifications = true;

    // 订阅时间
    @CreationTimestamp
    @Column(name = "date_subscribed", nullable = false)
    private LocalDateTime dateSubscribed;

    // 最后更新
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // 类型常量
    public static final String TYPE_NOTIFY = "NOTIFY";
    public static final String TYPE_MENTION_ONLY = "MENTION_ONLY";
    public static final String TYPE_MUTE = "MUTE";

    /**
     * 是否静音
     */
    public boolean isMuted() {
        return subscriptionType.equals(TYPE_MUTE);
    }

    /**
     * 是否仅提及
     */
    public boolean isMentionOnly() {
        return subscriptionType.equals(TYPE_MENTION_ONLY);
    }

    /**
     * 是否通知
     */
    public boolean shouldNotify() {
        return subscriptionType.equals(TYPE_NOTIFY);
    }
}
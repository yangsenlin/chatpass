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
 * Subscription 实体 - Zulip 订阅关系
 * 对应 Zulip Subscription model
 * 
 * 用户订阅 Stream 的关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subscriptions", uniqueConstraints = {
    @UniqueConstraint(name = "uniq_user_stream", columnNames = {"user_profile_id", "stream_id"})
})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    // 颜色 (UI 显示)
    @Column(name = "color")
    @Builder.Default
    private String color = "#c2c2c2";

    // 是否固定
    @Column(name = "pinned_to_top")
    @Builder.Default
    private Boolean pinnedToTop = false;

    // 桌面通知
    @Column(name = "desktop_notifications")
    private Boolean desktopNotifications;

    // 邮件通知
    @Column(name = "email_notifications")
    private Boolean emailNotifications;

    // 移动推送
    @Column(name = "push_notifications")
    private Boolean pushNotifications;

    // 可见性
    @Column(name = "is_muted")
    @Builder.Default
    private Boolean isMuted = false;

    // 在首页显示
    @Column(name = "in_home_view")
    @Builder.Default
    private Boolean inHomeView = true;

    // 活跃状态
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    // 最后更新时间
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;
}
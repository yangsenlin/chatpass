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
 * Stream 实体 - Zulip 频道/流
 * 对应 Zulip Stream model
 * 
 * Stream 是 Zulip 中的公共频道概念
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "streams")
public class Stream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private UserProfile creator;

    @Column(name = "deactivated")
    @Builder.Default
    private Boolean deactivated = false;

    @Column(name = "description", length = 1024)
    @Builder.Default
    private String description = "";

    @Column(name = "rendered_description")
    private String renderedDescription;

    // 订阅者数量
    @Column(name = "subscriber_count")
    @Builder.Default
    private Integer subscriberCount = 0;

    // 访问控制
    @Column(name = "invite_only")
    @Builder.Default
    private Boolean inviteOnly = false;

    @Column(name = "history_public_to_subscribers")
    @Builder.Default
    private Boolean historyPublicToSubscribers = true;

    @Column(name = "is_web_public")
    @Builder.Default
    private Boolean isWebPublic = false;

    // 消息发布策略
    // 1=Everyone, 2=Admins, 3=Restrict new members, 4=Moderators
    @Column(name = "stream_post_policy")
    @Builder.Default
    private Integer streamPostPolicy = 1;

    // 消息保留
    @Column(name = "message_retention_days")
    private Integer messageRetentionDays;

    // Topics 政策
    // 1=Inherit, 2=Allow empty topic, 3=Disable empty topic, 4=Empty topic only
    @Column(name = "topics_policy")
    @Builder.Default
    private Integer topicsPolicy = 1;

    // 关联 Recipient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    // 频道文件夹
    @Column(name = "folder_id")
    private Long folderId;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
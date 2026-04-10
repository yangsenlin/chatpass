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
 * Message 实体 - Zulip 消息
 * 对应 Zulip AbstractMessage model
 * 
 * Zulip 的核心实体：消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_messages_recipient", columnList = "recipient_id"),
    @Index(name = "idx_messages_sender", columnList = "sender_id"),
    @Index(name = "idx_messages_realm", columnList = "realm_id"),
    @Index(name = "idx_messages_date_sent", columnList = "date_sent")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 发送者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

    // 接收者 (通过 Recipient 关联 Stream 或 Private)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    // 所属 Realm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // 消息类型
    // 1=Normal, 2=Resolve topic notification
    @Column(name = "type")
    @Builder.Default
    private Integer type = 1;

    // Topic (主题) - Zulip 的核心概念
    // Direct messages 使用特殊字符 \x07 作为 topic
    @Column(name = "subject", length = 60)
    private String subject;

    // 消息内容
    @Column(name = "content", columnDefinition = "text")
    private String content;

    // 渲染后的 HTML
    @Column(name = "rendered_content", columnDefinition = "text")
    private String renderedContent;

    // 渲染版本
    @Column(name = "rendered_content_version")
    private Integer renderedContentVersion;

    // 发送时间
    @Column(name = "date_sent")
    private LocalDateTime dateSent;

    // 客户端信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sending_client_id")
    private Client sendingClient;

    // 编辑信息
    @Column(name = "last_edit_time")
    private LocalDateTime lastEditTime;

    @Column(name = "edit_history", columnDefinition = "text")
    private String editHistory;

    // 消息属性标志
    @Column(name = "has_attachment")
    @Builder.Default
    private Boolean hasAttachment = false;

    @Column(name = "has_image")
    @Builder.Default
    private Boolean hasImage = false;

    @Column(name = "has_link")
    @Builder.Default
    private Boolean hasLink = false;

    @Column(name = "is_channel_message")
    @Builder.Default
    private Boolean isChannelMessage = true;

    // Topic 名字 (便捷方法)
    public String getTopicName() {
        // DM 使用特殊字符，返回空字符串
        if (recipient != null && recipient.getType() != null && recipient.getType() == Recipient.TYPE_PRIVATE) {
            return "";
        }
        return subject;
    }

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
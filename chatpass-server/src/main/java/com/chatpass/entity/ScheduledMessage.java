package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ScheduledMessage 实体 - 定时消息
 * 
 * 支持用户定时发送消息或设置提醒
 * 
 * 功能：
 * - 定时发送频道消息或私聊消息
 * - 提醒功能（提醒用户回复某条消息）
 * - 记录发送状态和失败原因
 */
@Entity
@Table(name = "scheduled_messages", indexes = {
    @Index(name = "idx_scheduled_timestamp", columnList = "scheduled_timestamp"),
    @Index(name = "idx_sender_delivery", columnList = "sender_id, delivery_type, scheduled_timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发送者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

    /**
     * 接收者（频道或私聊群组）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    /**
     * 话题名称（仅频道消息）
     */
    @Column(name = "topic", length = 100)
    private String topic;

    /**
     * 消息内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 渲染后的内容（Markdown 渲染）
     */
    @Column(name = "rendered_content", columnDefinition = "TEXT")
    private String renderedContent;

    /**
     * 发送客户端
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client sendingClient;

    /**
     * 频道（仅频道消息）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private Stream stream;

    /**
     * 组织
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    /**
     * 定时发送时间
     */
    @Column(name = "scheduled_timestamp", nullable = false)
    private LocalDateTime scheduledTimestamp;

    /**
     * 发送者是否已读
     */
    @Column(name = "read_by_sender", nullable = false)
    @Builder.Default
    private Boolean readBySender = false;

    /**
     * 是否已发送
     */
    @Column(name = "delivered", nullable = false)
    @Builder.Default
    private Boolean delivered = false;

    /**
     * 发送后的消息（关联到实际发送的消息）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivered_message_id")
    private Message deliveredMessage;

    /**
     * 是否有附件
     */
    @Column(name = "has_attachment", nullable = false)
    @Builder.Default
    private Boolean hasAttachment = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "request_timestamp", nullable = false, updatable = false)
    private LocalDateTime requestTimestamp;

    /**
     * 发送类型
     * SEND_LATER: 定时发送
     * REMIND: 提醒
     */
    @Column(name = "delivery_type", nullable = false)
    @Builder.Default
    private Integer deliveryType = DeliveryType.SEND_LATER.getValue();

    /**
     * 提醒目标消息ID（仅提醒类型）
     * 提醒用户回复某条消息时记录原消息ID
     */
    @Column(name = "reminder_target_message_id")
    private Long reminderTargetMessageId;

    /**
     * 提醒备注（仅提醒类型）
     */
    @Column(name = "reminder_note", columnDefinition = "TEXT")
    private String reminderNote;

    /**
     * 是否发送失败
     */
    @Column(name = "failed", nullable = false)
    @Builder.Default
    private Boolean failed = false;

    /**
     * 失败原因
     */
    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    /**
     * 发送类型常量
     */
    public enum DeliveryType {
        SEND_LATER(1, "定时发送"),
        REMIND(2, "提醒");

        private final int value;
        private final String description;

        DeliveryType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查是否是频道消息
     */
    public boolean isStreamMessage() {
        return recipient != null && recipient.getType() == Recipient.TYPE_STREAM;
    }

    /**
     * 检查是否是私聊消息
     */
    public boolean isDirectMessage() {
        return recipient != null && recipient.getType() == Recipient.TYPE_PRIVATE;
    }

    /**
     * 获取话题名称
     */
    public String getTopicName() {
        return topic;
    }

    /**
     * 设置话题名称
     */
    public void setTopicName(String topicName) {
        this.topic = topicName;
    }
}
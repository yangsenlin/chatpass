package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * MessageEditHistory 实体 - 消息编辑历史
 * 对应 Zulip MessageEdit model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_edit_history", indexes = {
    @Index(name = "idx_edit_history_message", columnList = "message_id"),
    @Index(name = "idx_edit_history_editor", columnList = "editor_id"),
    @Index(name = "idx_edit_history_time", columnList = "edit_time")
})
public class MessageEditHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 原消息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // 编辑者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = false)
    private UserProfile editor;

    // 编辑前内容
    @Column(name = "prev_content", columnDefinition = "text")
    private String prevContent;

    // 编辑前 Topic
    @Column(name = "prev_topic", length = 60)
    private String prevTopic;

    // 编辑后内容
    @Column(name = "new_content", columnDefinition = "text")
    private String newContent;

    // 编辑后 Topic
    @Column(name = "new_topic", length = 60)
    private String newTopic;

    // 编辑时间
    @CreationTimestamp
    @Column(name = "edit_time", nullable = false)
    private LocalDateTime editTime;

    // 编辑类型: 1=content, 2=topic, 3=both
    @Column(name = "edit_type", nullable = false)
    @Builder.Default
    private Integer editType = 1;

    // 编辑类型常量
    public static final int EDIT_TYPE_CONTENT = 1;
    public static final int EDIT_TYPE_TOPIC = 2;
    public static final int EDIT_TYPE_BOTH = 3;

    /**
     * 判断是否编辑了内容
     */
    public boolean isContentEdit() {
        return editType == EDIT_TYPE_CONTENT || editType == EDIT_TYPE_BOTH;
    }

    /**
     * 判断是否编辑了 Topic
     */
    public boolean isTopicEdit() {
        return editType == EDIT_TYPE_TOPIC || editType == EDIT_TYPE_BOTH;
    }
}
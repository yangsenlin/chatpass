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
 * MessagePoll 实体 - 消息投票
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_polls", indexes = {
    @Index(name = "idx_poll_message", columnList = "message_id"),
    @Index(name = "idx_poll_status", columnList = "status"),
    @Index(name = "idx_poll_creator", columnList = "creator_id")
})
public class MessagePoll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联消息
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    // 创建者
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // 投票问题
    @Column(name = "question", nullable = false, length = 200)
    private String question;

    // 投票选项（JSON 数组）
    @Column(name = "options", nullable = false, columnDefinition = "TEXT")
    private String options;

    // 投票类型: SINGLE（单选）、MULTIPLE（多选）
    @Column(name = "poll_type", length = 20)
    @Builder.Default
    private String pollType = "SINGLE";

    // 是否匿名投票
    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    // 是否允许修改投票
    @Column(name = "allow_change")
    @Builder.Default
    private Boolean allowChange = true;

    // 结束时间（可选）
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // 状态: OPEN、CLOSED、ENDED
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "OPEN";

    // 总投票数
    @Column(name = "total_votes")
    @Builder.Default
    private Long totalVotes = 0L;

    // 创建时间
    @CreationTimestamp
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // 状态常量
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_ENDED = "ENDED";

    // 类型常量
    public static final String TYPE_SINGLE = "SINGLE";
    public static final String TYPE_MULTIPLE = "MULTIPLE";

    /**
     * 是否已结束
     */
    public boolean isEnded() {
        if (status.equals(STATUS_ENDED) || status.equals(STATUS_CLOSED)) {
            return true;
        }
        if (endTime != null && LocalDateTime.now().isAfter(endTime)) {
            return true;
        }
        return false;
    }

    /**
     * 结束投票
     */
    public void endPoll() {
        this.status = STATUS_ENDED;
        this.endTime = LocalDateTime.now();
    }
}
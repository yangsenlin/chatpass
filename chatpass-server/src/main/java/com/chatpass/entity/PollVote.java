package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * PollVote 实体 - 投票记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "poll_votes", indexes = {
    @Index(name = "idx_vote_poll", columnList = "poll_id"),
    @Index(name = "idx_vote_user", columnList = "user_id"),
    @Index(name = "idx_vote_option", columnList = "option_index")
}, uniqueConstraints = {
    // 单选投票：poll_id + user_id 唯一
    // 多选投票：poll_id + user_id + option_index 唯一（通过业务逻辑控制）
})
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 投票
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private MessagePoll poll;

    // 投票者
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 选项索引（从 0 开始）
    @Column(name = "option_index", nullable = false)
    private Integer optionIndex;

    // 选项内容（快照）
    @Column(name = "option_text", length = 200)
    private String optionText;

    // 投票时间
    @CreationTimestamp
    @Column(name = "vote_time", nullable = false)
    private LocalDateTime voteTime;

    // 是否已取消
    @Column(name = "is_cancelled")
    @Builder.Default
    private Boolean isCancelled = false;
}
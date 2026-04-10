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
 * Draft 实体 - 消息草稿
 * 
 * 保存用户未发送的消息草稿
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "drafts", indexes = {
    @Index(name = "idx_drafts_user", columnList = "user_profile_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uniq_user_recipient_topic", 
            columnNames = {"user_profile_id", "recipient_id", "topic"})
})
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    @Column(name = "topic", length = 60)
    private String topic;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "last_edit_time")
    private LocalDateTime lastEditTime;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
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
 * MessageLink 实体 - 消息引用链接
 * 
 * 记录消息之间的引用关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_links", indexes = {
    @Index(name = "idx_message_links_message", columnList = "message_id"),
    @Index(name = "idx_message_links_target", columnList = "target_message_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uniq_message_target", columnNames = {"message_id", "target_message_id"})
})
public class MessageLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_message_id", nullable = false)
    private Message targetMessage;

    // 链接类型：reply, forward, reference
    @Column(name = "link_type", length = 20)
    @Builder.Default
    private String linkType = LINK_TYPE_REFERENCE;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public static final String LINK_TYPE_REFERENCE = "reference";
    public static final String LINK_TYPE_REPLY = "reply";
    public static final String LINK_TYPE_FORWARD = "forward";
}
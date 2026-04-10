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
 * Reaction 实体 - 消息表情反应
 * 
 * 用户可以对消息添加表情反应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reactions", indexes = {
    @Index(name = "idx_reactions_message", columnList = "message_id"),
    @Index(name = "idx_reactions_user", columnList = "user_profile_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uniq_user_message_emoji", columnNames = {"user_profile_id", "message_id", "emoji_code"})
})
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // Emoji 代码（Unicode 或别名）
    @Column(name = "emoji_code", nullable = false, length = 50)
    private String emojiCode;

    // Emoji 名称（显示用）
    @Column(name = "emoji_name", length = 100)
    private String emojiName;

    // Emoji 类型：unicode, realm_emoji, zulip_extra_emoji
    @Column(name = "emoji_type", length = 20)
    @Builder.Default
    private String emojiType = "unicode";

    // 反应类型（预留扩展）
    @Column(name = "reaction_type")
    @Builder.Default
    private Integer reactionType = 1;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
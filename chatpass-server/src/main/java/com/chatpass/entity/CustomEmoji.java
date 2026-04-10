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
 * CustomEmoji 实体 - 自定义表情
 * 对应 Zulip RealmEmoji model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "custom_emojis", indexes = {
    @Index(name = "idx_emojis_realm", columnList = "realm_id"),
    @Index(name = "idx_emojis_name", columnList = "name"),
    @Index(name = "idx_emojis_author", columnList = "author_id")
})
public class CustomEmoji {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 所属 Realm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // 表情名称（用于消息中引用，如 :custom_emoji:）
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    // 表情显示名称
    @Column(name = "display_name", length = 128)
    private String displayName;

    // 表情图片 URL
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // 作者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserProfile author;

    // 是否 deactivated（已删除）
    @Column(name = "deactivated", nullable = false)
    @Builder.Default
    private Boolean deactivated = false;

    // 创建时间
    @CreationTimestamp
    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    /**
     * 获取表情引用字符串
     */
    public String getEmojiCode() {
        return ":" + name + ":";
    }

    /**
     * 是否可用
     */
    public boolean isActive() {
        return !deactivated;
    }
}
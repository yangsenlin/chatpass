package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * SavedSnippet 实体 - 保存片段
 * 
 * 用户预设的回复片段，可以快速插入到消息中
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "saved_snippets", indexes = {
    @Index(name = "idx_saved_snippet_user", columnList = "user_profile_id"),
    @Index(name = "idx_saved_snippet_realm", columnList = "realm_id")
})
public class SavedSnippet {

    public static final int MAX_TITLE_LENGTH = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属组织
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    /**
     * 所属用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    /**
     * 片段标题
     */
    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    /**
     * 片段内容
     */
    @Lob
    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;
}
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
 * NavigationView 实体 - 导航视图
 * 
 * 用户在左侧导航栏中固定的视图配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "navigation_views", uniqueConstraints = {
    @UniqueConstraint(name = "uniq_user_fragment", columnNames = {"user_profile_id", "fragment"})
})
public class NavigationView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    /**
     * 视图的 URL 片段标识符
     * 例如: 'inbox', 'narrow/has/reactions'
     */
    @Column(nullable = false)
    private String fragment;

    /**
     * 是否固定在导航栏
     */
    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    /**
     * 视图的显示名称（用户自定义）
     */
    @Column
    private String name;

    /**
     * 视图类型（预留字段）
     * 例如: inbox, starred, mentions, drafts
     */
    @Column(name = "view_type")
    private String viewType;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
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
 * AlertWord 实体 - 用户自定义关键词提醒
 * 
 * 用户可以设置关键词，当消息包含这些关键词时会收到通知
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alert_words", indexes = {
    @Index(name = "idx_alert_words_user", columnList = "user_profile_id"),
    @Index(name = "idx_alert_words_realm", columnList = "realm_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uniq_user_word", columnNames = {"user_profile_id", "word"})
})
public class AlertWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // 关键词（不区分大小写匹配）
    @Column(name = "word", nullable = false, length = 100)
    private String word;

    // 是否启用
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // 匹配模式
    // 1=Contains (默认), 2=Exact match, 3=Starts with, 4=Ends with
    @Column(name = "match_mode")
    @Builder.Default
    private Integer matchMode = 1;

    // 通知方式
    @Column(name = "notify_email")
    @Builder.Default
    private Boolean notifyEmail = false;

    @Column(name = "notify_push")
    @Builder.Default
    private Boolean notifyPush = true;

    @Column(name = "notify_desktop")
    @Builder.Default
    private Boolean notifyDesktop = true;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // 匹配模式常量
    public static final int MODE_CONTAINS = 1;
    public static final int MODE_EXACT = 2;
    public static final int MODE_STARTS_WITH = 3;
    public static final int MODE_ENDS_WITH = 4;
}
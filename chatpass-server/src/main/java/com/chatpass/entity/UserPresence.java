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
 * UserPresence 实体 - 用户在线状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_presences", indexes = {
    @Index(name = "idx_presence_user", columnList = "user_profile_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uniq_user_presence", columnNames = {"user_profile_id"})
})
public class UserPresence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    // 状态：active, idle, offline
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "offline";

    // 最后活跃时间
    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;

    // 最后推送时间
    @Column(name = "last_push_received_time")
    private LocalDateTime lastPushReceivedTime;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_IDLE = "idle";
    public static final String STATUS_OFFLINE = "offline";
}
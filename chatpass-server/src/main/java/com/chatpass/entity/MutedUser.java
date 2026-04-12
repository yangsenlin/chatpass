package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * MutedUser 实体 - 用户屏蔽
 * 
 * 允许用户屏蔽特定用户的消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "muted_users", uniqueConstraints = {
    @UniqueConstraint(name = "uniq_muter_muted", 
            columnNames = {"user_profile_id", "muted_user_id"})
})
public class MutedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发起屏蔽的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    /**
     * 被屏蔽的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muted_user_id", nullable = false)
    private UserProfile mutedUser;

    @CreationTimestamp
    @Column(name = "date_muted", nullable = false, updatable = false)
    private LocalDateTime dateMuted;
}
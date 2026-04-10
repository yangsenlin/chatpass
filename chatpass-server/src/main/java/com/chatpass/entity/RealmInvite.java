package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RealmInvite 实体
 * 
 * 组织邀请链接
 */
@Entity
@Table(name = "realm_invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealmInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_user_id", nullable = false)
    private UserProfile invitedByUser;

    @Column(name = "invite_link", nullable = false, unique = true, length = 64)
    private String inviteLink;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = STATUS_PENDING;

    // 状态常量
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_ACCEPTED = 2;
    public static final int STATUS_EXPIRED = 3;
    public static final int STATUS_REVOKED = 4;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_user_id")
    private UserProfile acceptedUser;

    @Column(name = "max_uses")
    @Builder.Default
    private Integer maxUses = 1;

    @Column(name = "current_uses")
    @Builder.Default
    private Integer currentUses = 0;

    @Column(name = "date_created", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreated = LocalDateTime.now();
}
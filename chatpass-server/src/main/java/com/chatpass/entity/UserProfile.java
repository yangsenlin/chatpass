package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * UserProfile 实体 - Zulip 用户
 * 对应 Zulip UserProfile model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // 认证信息
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "api_key")
    private String apiKey;

    // 基本信息
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "avatar_source")
    @Builder.Default
    private Integer avatarSource = 0; // 0=Gravatar, 1=User uploaded

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "avatar_version")
    @Builder.Default
    private Integer avatarVersion = 1;

    // 用户角色
    @Column(name = "role")
    @Builder.Default
    private Integer role = 100; // 100=User, 200=Moderator, 300=Admin, 400=Owner

    // 时区
    @Column(name = "timezone")
    @Builder.Default
    private String timezone = "UTC";

    // 语言
    @Column(name = "default_language")
    @Builder.Default
    private String defaultLanguage = "en";

    // 交付设置
    @Column(name = "delivery_email")
    @Builder.Default
    private Boolean deliveryEmail = true;

    // 桌面通知
    @Column(name = "enable_desktop_notifications")
    @Builder.Default
    private Boolean enableDesktopNotifications = true;

    @Column(name = "enable_sounds")
    @Builder.Default
    private Boolean enableSounds = true;

    // 邮件通知
    @Column(name = "enable_offline_email_notifications")
    @Builder.Default
    private Boolean enableOfflineEmailNotifications = true;

    // 移动推送
    @Column(name = "enable_offline_push_notifications")
    @Builder.Default
    private Boolean enableOfflinePushNotifications = true;

    // 消息设置
    @Column(name = "enter_sends")
    @Builder.Default
    private Boolean enterSends = false;

    @Column(name = "fluid_layout_width")
    @Builder.Default
    private Boolean fluidLayoutWidth = false;

    @Column(name = "twenty_four_hour_time")
    @Builder.Default
    private Boolean twentyFourHourTime = false;

    // 颜色主题
    @Column(name = "color_scheme")
    @Builder.Default
    private Integer colorScheme = 1; // 1=Auto, 2=Dark, 3=Light

    // Web 设置
    @Column(name = "web_home_view")
    @Builder.Default
    private String webHomeView = "inbox";

    // Bot 相关
    @Column(name = "bot_type")
    private Integer botType; // null=Human, 1=Generic, 2=Incoming webhook, 3=Outgoing webhook

    @Column(name = "bot_owner_id")
    private Long botOwnerId;

    // 激活状态
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_mirror_dummy")
    @Builder.Default
    private Boolean isMirrorDummy = false;

    @Column(name = "is_system_bot")
    @Builder.Default
    private Boolean isSystemBot = false;

    // 游客模式
    @Column(name = "is_guest")
    @Builder.Default
    private Boolean isGuest = false;

    // 个性化设置
    @Column(name = "custom_profile_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> customProfileData = new HashMap<>();

    // 最后登录
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // 邀请信息
    @Column(name = "referred_by_id")
    private Long referredById;

    @Column(name = "invited_by_id")
    private Long invitedById;

    // 关机模式
    @Column(name = "is_billing_admin")
    @Builder.Default
    private Boolean isBillingAdmin = false;

    // 限制日期
    @Column(name = "onboarding_steps", columnDefinition = "jsonb")
    private String onboardingSteps;

    @CreationTimestamp
    @Column(name = "date_joined", nullable = false, updatable = false)
    private LocalDateTime dateJoined;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
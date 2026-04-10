package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MobilePush 实体
 * 移动推送配置
 */
@Entity
@Table(name = "mobile_push_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobilePush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "realm_id", nullable = false)
    private Long realmId;

    @Column(name = "push_type", nullable = false, length = 20)
    private String pushType;

    @Column(name = "project_id", length = 100)
    private String projectId;

    @Column(name = "api_key", length = 200)
    private String apiKey;

    @Column(name = "sender_id", length = 100)
    private String senderId;

    @Column(name = "certificate_path", length = 200)
    private String certificatePath;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "batch_size")
    @Builder.Default
    private Integer batchSize = 100;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Push types
    public static final String TYPE_FCM = "FCM";
    public static final String TYPE_APNS = "APNS";
}
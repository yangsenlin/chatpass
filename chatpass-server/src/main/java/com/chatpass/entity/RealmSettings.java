package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 组织配置实体
 * 用于存储组织的各类配置项
 */
@Entity
@Table(name = "realm_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealmSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 组织ID
     */
    @Column(name = "realm_id", nullable = false, unique = true)
    private Long realmId;
    
    /**
     * 配置键
     */
    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;
    
    /**
     * 配置值
     */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;
    
    /**
     * 配置类型
     */
    @Column(name = "setting_type", length = 20)
    @Builder.Default
    private String settingType = "string"; // string, boolean, integer, json
    
    /**
     * 配置描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 是否可修改
     */
    @Column(name = "editable")
    @Builder.Default
    private Boolean editable = true;
    
    /**
     * 是否公开
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 组织域名实体
 * 用于管理组织的自定义域名配置
 */
@Entity
@Table(name = "realm_domains")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealmDomain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 域名
     */
    @Column(nullable = false, unique = true, length = 255)
    private String domain;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id", nullable = false)
    private Long realmId;
    
    /**
     * 是否为主要域名
     */
    @Column(name = "is_primary")
    private Boolean isPrimary = false;
    
    /**
     * 是否允许子域名
     */
    @Column(name = "allow_subdomains")
    private Boolean allowSubdomains = false;
    
    /**
     * 域名状态
     */
    @Column(name = "status", length = 20)
    private String status = "pending"; // pending, verified, failed
    
    /**
     * 验证时间
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
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

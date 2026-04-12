package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 频道分类实体
 * 用于组织和管理频道的分组
 */
@Entity
@Table(name = "channel_folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelFolder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 分类名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 分类描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id", nullable = false)
    private Long realmId;
    
    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    /**
     * 是否为默认分类
     */
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    /**
     * 创建者ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
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

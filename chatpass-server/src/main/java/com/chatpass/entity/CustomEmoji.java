package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 自定义表情实体
 * 用于组织上传的自定义表情
 */
@Entity
@Table(name = "custom_emojis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomEmoji {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 表情名称（用于引用）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    /**
     * 表情别名（逗号分隔）
     */
    @Column(name = "aliases", length = 500)
    private String aliases;
    
    /**
     * 图片URL
     */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    /**
     * 图片路径
     */
    @Column(name = "image_path", length = 500)
    private String imagePath;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id", nullable = false)
    private Long realmId;
    
    /**
     * 创建者ID
     */
    @Column(name = "author_id")
    private Long authorId;
    
    /**
     * 是否 deactivated
     */
    @Column(name = "deactivated")
    @Builder.Default
    private Boolean deactivated = false;
    
    /**
     * 使用次数
     */
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;
    
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

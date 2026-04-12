package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Stream权限实体
 * 用于管理Stream的访问权限
 */
@Entity
@Table(name = "stream_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Stream ID
     */
    @Column(name = "stream_id", nullable = false)
    private Long streamId;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 权限类型
     */
    @Column(name = "permission_type", length = 20)
    @Builder.Default
    private String permissionType = "member"; // owner, admin, member, guest
    
    /**
     * 是否可以读取
     */
    @Column(name = "can_read")
    @Builder.Default
    private Boolean canRead = true;
    
    /**
     * 是否可以发送消息
     */
    @Column(name = "can_write")
    @Builder.Default
    private Boolean canWrite = true;
    
    /**
     * 是否可以修改话题
     */
    @Column(name = "can_modify_topic")
    @Builder.Default
    private Boolean canModifyTopic = true;
    
    /**
     * 是否可以管理成员
     */
    @Column(name = "can_manage_members")
    @Builder.Default
    private Boolean canManageMembers = false;
    
    /**
     * 是否可以删除消息
     */
    @Column(name = "can_delete_messages")
    @Builder.Default
    private Boolean canDeleteMessages = false;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
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

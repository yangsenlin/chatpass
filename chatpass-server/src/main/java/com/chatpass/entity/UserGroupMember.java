package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 用户组成员实体
 * 记录用户与组的关系
 */
@Entity
@Table(name = "user_group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户组ID
     */
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 成员角色
     */
    @Column(name = "role", length = 20)
    @Builder.Default
    private String role = "member"; // owner, admin, member
    
    /**
     * 是否为主人
     */
    @Column(name = "is_owner")
    @Builder.Default
    private Boolean isOwner = false;
    
    /**
     * 加入时间
     */
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

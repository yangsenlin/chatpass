package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RolePermission 实体
 * 
 * 角色-权限关联
 */
@Entity
@Table(name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role", "permission_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role", nullable = false)
    private Integer role; // 用户角色：100=User, 200=Moderator, 300=Admin, 400=Owner

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    // 角色常量（与 UserProfile.role 对应）
    public static final int ROLE_USER = 100;
    public static final int ROLE_MODERATOR = 200;
    public static final int ROLE_ADMIN = 300;
    public static final int ROLE_OWNER = 400;
}
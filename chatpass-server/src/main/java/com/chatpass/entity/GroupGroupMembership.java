package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * GroupGroupMembership 实体 - 用户组嵌套关系
 * 
 * 允许用户组包含其他用户组，形成层级关系
 * 例如：管理员组可以是"全体员工组"的子组
 */
@Entity
@Table(name = "group_group_memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uniq_supergroup_subgroup",
                columnNames = {"supergroup_id", "subgroup_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupGroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 父组（超级组）
     * 子组的成员自动成为父组的成员
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supergroup_id", nullable = false)
    private UserGroup supergroup;

    /**
     * 子组
     * 子组的成员会自动继承父组的权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id", nullable = false)
    private UserGroup subgroup;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 防止循环引用：
     * 一个组不能同时是另一个组的父组和子组
     */
    @PrePersist
    @PreUpdate
    protected void validateNoCycle() {
        if (supergroup != null && subgroup != null) {
            if (supergroup.getId().equals(subgroup.getId())) {
                throw new IllegalStateException("组不能包含自己");
            }
        }
    }
}
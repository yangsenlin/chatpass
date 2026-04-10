package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission 实体
 * 
 * 权限定义
 */
@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code; // e.g., "send_message", "create_stream"

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 显示名称

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", length = 50)
    private String category; // 分类：message, stream, user, admin

    // 权限分类常量
    public static final String CATEGORY_MESSAGE = "message";
    public static final String CATEGORY_STREAM = "stream";
    public static final String CATEGORY_USER = "user";
    public static final String CATEGORY_ADMIN = "admin";

    // 常用权限定义
    public static final String SEND_MESSAGE = "send_message";
    public static final String CREATE_STREAM = "create_stream";
    public static final String DELETE_MESSAGE = "delete_message";
    public static final String EDIT_MESSAGE = "edit_message";
    public static final String MANAGE_USERS = "manage_users";
    public static final String MANAGE_STREAMS = "manage_streams";
    public static final String ADMIN_ALL = "admin_all";
}
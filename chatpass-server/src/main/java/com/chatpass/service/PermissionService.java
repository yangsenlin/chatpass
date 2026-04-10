package com.chatpass.service;

import com.chatpass.entity.Permission;
import com.chatpass.entity.RolePermission;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.PermissionRepository;
import com.chatpass.repository.RolePermissionRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PermissionService
 * 
 * 权限管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserProfileRepository userRepository;

    /**
     * 检查用户是否有某个权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        UserProfile user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        
        // 获取用户角色
        Integer userRole = user.getRole() != null ? user.getRole() : 100;
        
        // 查找权限
        Permission permission = permissionRepository.findByCode(permissionCode).orElse(null);
        if (permission == null) return false;
        
        // 检查角色是否有此权限
        return rolePermissionRepository.existsByRoleAndPermission(userRole, permission);
    }

    /**
     * 检查用户角色是否有某个权限
     */
    public boolean hasPermissionByRole(Integer role, String permissionCode) {
        Permission permission = permissionRepository.findByCode(permissionCode).orElse(null);
        if (permission == null) return false;
        
        return rolePermissionRepository.existsByRoleAndPermission(role, permission);
    }

    /**
     * 获取用户的所有权限
     */
    public List<Permission> getUserPermissions(Long userId) {
        UserProfile user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        
        Integer userRole = user.getRole() != null ? user.getRole() : 100;
        
        return rolePermissionRepository.findPermissionsByRole(userRole);
    }

    /**
     * 获取角色的所有权限
     */
    public List<Permission> getRolePermissions(Integer role) {
        return rolePermissionRepository.findPermissionsByRole(role);
    }

    /**
     * 创建权限
     */
    @Transactional
    public Permission createPermission(String code, String name, String description, String category) {
        if (permissionRepository.existsByCode(code)) {
            throw new IllegalArgumentException("权限已存在: " + code);
        }
        
        Permission permission = Permission.builder()
                .code(code)
                .name(name)
                .description(description)
                .category(category)
                .build();
        
        permissionRepository.save(permission);
        log.info("Created permission: {}", code);
        
        return permission;
    }

    /**
     * 为角色授予权限
     */
    @Transactional
    public void grantPermission(Integer role, Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("权限不存在"));
        
        if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            log.debug("Role {} already has permission {}", role, permission.getCode());
            return;
        }
        
        RolePermission rp = RolePermission.builder()
                .role(role)
                .permission(permission)
                .build();
        
        rolePermissionRepository.save(rp);
        log.info("Granted permission {} to role {}", permission.getCode(), role);
    }

    /**
     * 为角色授予权限（通过权限代码）
     */
    @Transactional
    public void grantPermissionByCode(Integer role, String permissionCode) {
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new IllegalArgumentException("权限不存在: " + permissionCode));
        
        grantPermission(role, permission.getId());
    }

    /**
     * 移除角色的权限
     */
    @Transactional
    public void revokePermission(Integer role, Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("权限不存在"));
        
        rolePermissionRepository.deleteByRoleAndPermission(role, permission);
        log.info("Revoked permission {} from role {}", permission.getCode(), role);
    }

    /**
     * 获取所有权限
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllOrderByCategoryAndName();
    }

    /**
     * 获取某分类的所有权限
     */
    public List<Permission> getPermissionsByCategory(String category) {
        return permissionRepository.findByCategory(category);
    }

    /**
     * 初始化默认权限
     */
    @Transactional
    public void initDefaultPermissions() {
        // 消息权限
        createPermission(Permission.SEND_MESSAGE, "发送消息", "允许发送消息", Permission.CATEGORY_MESSAGE);
        createPermission(Permission.DELETE_MESSAGE, "删除消息", "允许删除消息", Permission.CATEGORY_MESSAGE);
        createPermission(Permission.EDIT_MESSAGE, "编辑消息", "允许编辑消息", Permission.CATEGORY_MESSAGE);
        
        // Stream 权限
        createPermission(Permission.CREATE_STREAM, "创建频道", "允许创建频道", Permission.CATEGORY_STREAM);
        createPermission(Permission.MANAGE_STREAMS, "管理频道", "允许管理频道", Permission.CATEGORY_STREAM);
        
        // 用户权限
        createPermission(Permission.MANAGE_USERS, "管理用户", "允许管理用户", Permission.CATEGORY_USER);
        
        // 管理员权限
        createPermission(Permission.ADMIN_ALL, "超级管理员", "所有权限", Permission.CATEGORY_ADMIN);
        
        // 默认角色权限分配
        // 普通用户（100）
        grantPermissionByCode(RolePermission.ROLE_USER, Permission.SEND_MESSAGE);
        grantPermissionByCode(RolePermission.ROLE_USER, Permission.EDIT_MESSAGE);
        
        // 版主（200）
        grantPermissionByCode(RolePermission.ROLE_MODERATOR, Permission.SEND_MESSAGE);
        grantPermissionByCode(RolePermission.ROLE_MODERATOR, Permission.EDIT_MESSAGE);
        grantPermissionByCode(RolePermission.ROLE_MODERATOR, Permission.DELETE_MESSAGE);
        grantPermissionByCode(RolePermission.ROLE_MODERATOR, Permission.CREATE_STREAM);
        
        // 管理员（300）
        grantPermissionByCode(RolePermission.ROLE_ADMIN, Permission.SEND_MESSAGE);
        grantPermissionByCode(RolePermission.ROLE_ADMIN, Permission.EDIT_MESSAGE);
        grantPermissionByCode(RolePermission.ROLE_ADMIN, Permission.DELETE_MESSAGE);
        grantPermissionByCode(RolePermission.ROLE_ADMIN, Permission.CREATE_STREAM);
        grantPermissionByCode(RolePermission.ROLE_ADMIN, Permission.MANAGE_STREAMS);
        grantPermissionByCode(RolePermission.ROLE_ADMIN, Permission.MANAGE_USERS);
        grantPermissionByCode(RolePermission.ROLE_ADMIN, Permission.ADMIN_ALL);
        
        // 所有者（400）
        grantPermissionByCode(RolePermission.ROLE_OWNER, Permission.ADMIN_ALL);
        
        log.info("Default permissions initialized");
    }
}
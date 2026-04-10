package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.PermissionDTO;
import com.chatpass.entity.Permission;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Permission 控制器
 * 
 * 权限管理 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "权限管理 API")
public class PermissionController {

    private final PermissionService permissionService;
    private final SecurityUtil securityUtil;

    @GetMapping("/permissions")
    @Operation(summary = "获取所有权限")
    public ResponseEntity<ApiResponse<List<PermissionDTO.Response>>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        
        List<PermissionDTO.Response> responses = permissions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/permissions/{category}")
    @Operation(summary = "获取某分类的权限")
    public ResponseEntity<ApiResponse<List<PermissionDTO.Response>>> getPermissionsByCategory(
            @PathVariable String category) {
        List<Permission> permissions = permissionService.getPermissionsByCategory(category);
        
        List<PermissionDTO.Response> responses = permissions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/users/me/permissions")
    @Operation(summary = "获取当前用户的权限")
    public ResponseEntity<ApiResponse<List<PermissionDTO.Response>>> getCurrentUserPermissions() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<Permission> permissions = permissionService.getUserPermissions(userId);
        
        List<PermissionDTO.Response> responses = permissions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/users/me/permissions/{code}")
    @Operation(summary = "检查当前用户是否有某权限")
    public ResponseEntity<ApiResponse<Boolean>> checkPermission(@PathVariable String code) {
        Long userId = securityUtil.getCurrentUserId();
        
        boolean has = permissionService.hasPermission(userId, code);
        
        return ResponseEntity.ok(ApiResponse.success(has));
    }

    @GetMapping("/roles/{role}/permissions")
    @Operation(summary = "获取角色的权限")
    public ResponseEntity<ApiResponse<List<PermissionDTO.Response>>> getRolePermissions(
            @PathVariable Integer role) {
        List<Permission> permissions = permissionService.getRolePermissions(role);
        
        List<PermissionDTO.Response> responses = permissions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/permissions")
    @Operation(summary = "创建权限（管理员）")
    public ResponseEntity<ApiResponse<PermissionDTO.Response>> createPermission(
            @RequestBody PermissionDTO.CreateRequest request) {
        // 检查管理员权限
        if (!securityUtil.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error("FORBIDDEN", "需要管理员权限"));
        }
        
        Permission permission = permissionService.createPermission(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getCategory());
        
        return ResponseEntity.ok(ApiResponse.success(toResponse(permission)));
    }

    @PostMapping("/roles/{role}/permissions/{permissionId}")
    @Operation(summary = "为角色授予权限（管理员）")
    public ResponseEntity<ApiResponse<Void>> grantPermission(
            @PathVariable Integer role,
            @PathVariable Long permissionId) {
        // 检查管理员权限
        if (!securityUtil.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error("FORBIDDEN", "需要管理员权限"));
        }
        
        permissionService.grantPermission(role, permissionId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/roles/{role}/permissions/{permissionId}")
    @Operation(summary = "移除角色权限（管理员）")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable Integer role,
            @PathVariable Long permissionId) {
        // 检查管理员权限
        if (!securityUtil.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error("FORBIDDEN", "需要管理员权限"));
        }
        
        permissionService.revokePermission(role, permissionId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/permissions/init")
    @Operation(summary = "初始化默认权限（管理员）")
    public ResponseEntity<ApiResponse<Map<String, String>>> initPermissions() {
        // 检查管理员权限
        if (!securityUtil.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error("FORBIDDEN", "需要管理员权限"));
        }
        
        permissionService.initDefaultPermissions();
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "默认权限已初始化")));
    }

    private PermissionDTO.Response toResponse(Permission permission) {
        return PermissionDTO.Response.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .description(permission.getDescription())
                .category(permission.getCategory())
                .build();
    }
}
package com.chatpass.controller.api.v1;

import com.chatpass.dto.StreamPermissionDTO;
import com.chatpass.service.StreamPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Stream权限控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class StreamPermissionController {
    
    private final StreamPermissionService permissionService;
    
    /**
     * 添加用户权限
     */
    @PostMapping("/streams/{streamId}/permissions")
    public ResponseEntity<StreamPermissionDTO> addPermission(
            @PathVariable Long streamId,
            @RequestParam Long userId,
            @RequestParam String permissionType,
            @RequestParam(required = false) Long realmId) {
        
        StreamPermissionDTO permission = permissionService.addPermission(streamId, userId, permissionType, realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }
    
    /**
     * 更新权限
     */
    @PatchMapping("/streams/{streamId}/permissions/{userId}")
    public ResponseEntity<StreamPermissionDTO> updatePermission(
            @PathVariable Long streamId,
            @PathVariable Long userId,
            @RequestParam(required = false) String permissionType,
            @RequestParam(required = false) Boolean canRead,
            @RequestParam(required = false) Boolean canWrite,
            @RequestParam(required = false) Boolean canModifyTopic,
            @RequestParam(required = false) Boolean canManageMembers,
            @RequestParam(required = false) Boolean canDeleteMessages) {
        
        StreamPermissionDTO permission = permissionService.updatePermission(streamId, userId, 
                permissionType, canRead, canWrite, canModifyTopic, canManageMembers, canDeleteMessages);
        return ResponseEntity.ok(permission);
    }
    
    /**
     * 删除权限
     */
    @DeleteMapping("/streams/{streamId}/permissions/{userId}")
    public ResponseEntity<Void> removePermission(
            @PathVariable Long streamId,
            @PathVariable Long userId) {
        
        permissionService.removePermission(streamId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取Stream的所有权限
     */
    @GetMapping("/streams/{streamId}/permissions")
    public ResponseEntity<List<StreamPermissionDTO>> getStreamPermissions(@PathVariable Long streamId) {
        List<StreamPermissionDTO> permissions = permissionService.getStreamPermissions(streamId);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * 获取用户的权限
     */
    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<List<StreamPermissionDTO>> getUserPermissions(@PathVariable Long userId) {
        List<StreamPermissionDTO> permissions = permissionService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * 获取用户对Stream的权限
     */
    @GetMapping("/streams/{streamId}/permissions/{userId}")
    public ResponseEntity<StreamPermissionDTO> getPermission(
            @PathVariable Long streamId,
            @PathVariable Long userId) {
        
        return permissionService.getPermission(streamId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 检查用户是否有权限
     */
    @GetMapping("/streams/{streamId}/permissions/{userId}/check")
    public ResponseEntity<Boolean> hasPermission(
            @PathVariable Long streamId,
            @PathVariable Long userId) {
        
        boolean hasPermission = permissionService.hasPermission(streamId, userId);
        return ResponseEntity.ok(hasPermission);
    }
    
    /**
     * 检查读权限
     */
    @GetMapping("/streams/{streamId}/permissions/{userId}/can_read")
    public ResponseEntity<Boolean> canRead(
            @PathVariable Long streamId,
            @PathVariable Long userId) {
        
        boolean canRead = permissionService.canRead(streamId, userId);
        return ResponseEntity.ok(canRead);
    }
    
    /**
     * 检查写权限
     */
    @GetMapping("/streams/{streamId}/permissions/{userId}/can_write")
    public ResponseEntity<Boolean> canWrite(
            @PathVariable Long streamId,
            @PathVariable Long userId) {
        
        boolean canWrite = permissionService.canWrite(streamId, userId);
        return ResponseEntity.ok(canWrite);
    }
    
    /**
     * 获取Stream的管理员
     */
    @GetMapping("/streams/{streamId}/admins")
    public ResponseEntity<List<StreamPermissionDTO>> getStreamAdmins(@PathVariable Long streamId) {
        List<StreamPermissionDTO> admins = permissionService.getStreamAdmins(streamId);
        return ResponseEntity.ok(admins);
    }
    
    /**
     * 统计成员数量
     */
    @GetMapping("/streams/{streamId}/member_count")
    public ResponseEntity<Long> countMembers(@PathVariable Long streamId) {
        long count = permissionService.countMembers(streamId);
        return ResponseEntity.ok(count);
    }
}

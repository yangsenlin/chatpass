package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.StreamPermissionDTO;
import com.chatpass.entity.Stream;
import com.chatpass.entity.UserProfile;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.StreamPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StreamPermission 控制器
 * 
 * 频道权限管理 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Stream Permissions", description = "频道权限管理 API")
public class StreamPermissionController {

    private final StreamPermissionService permissionService;
    private final SecurityUtil securityUtil;

    @GetMapping("/streams/{streamId}/permissions")
    @Operation(summary = "获取频道权限设置")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStreamPermissions(
            @PathVariable Long streamId) {
        Map<String, Object> settings = permissionService.getStreamPermissionSettings(streamId);
        
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/streams/{streamId}/permissions")
    @Operation(summary = "更新频道权限设置")
    public ResponseEntity<ApiResponse<StreamPermissionDTO.PermissionResponse>> updateStreamPermissions(
            @PathVariable Long streamId,
            @RequestBody StreamPermissionDTO.UpdateRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        if (!permissionService.canManageStream(streamId, userId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("FORBIDDEN", "需要管理员权限"));
        }
        
        Stream stream = permissionService.updateStreamPermission(streamId, request.getInviteOnly());
        
        return ResponseEntity.ok(ApiResponse.success(StreamPermissionDTO.PermissionResponse.builder()
                .streamId(streamId)
                .streamName(stream.getName())
                .inviteOnly(stream.getInviteOnly())
                .build()));
    }

    @GetMapping("/streams/{streamId}/can_access")
    @Operation(summary = "检查用户是否可以访问频道")
    public ResponseEntity<ApiResponse<Boolean>> canAccessStream(
            @PathVariable Long streamId,
            @RequestParam Long userId) {
        boolean canAccess = permissionService.canAccessStream(streamId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(canAccess));
    }

    @GetMapping("/streams/{streamId}/can_post")
    @Operation(summary = "检查用户是否可以发消息")
    public ResponseEntity<ApiResponse<Boolean>> canPostToStream(
            @PathVariable Long streamId,
            @RequestParam Long userId) {
        boolean canPost = permissionService.canPostToStream(streamId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(canPost));
    }

    @GetMapping("/streams/{streamId}/can_manage")
    @Operation(summary = "检查用户是否可以管理频道")
    public ResponseEntity<ApiResponse<Boolean>> canManageStream(
            @PathVariable Long streamId,
            @RequestParam Long userId) {
        boolean canManage = permissionService.canManageStream(streamId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(canManage));
    }

    @GetMapping("/users/me/accessible_streams")
    @Operation(summary = "获取当前用户可访问的频道")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserAccessibleStreams() {
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<Stream> streams = permissionService.getUserAccessibleStreams(userId, realmId);
        
        List<Map<String, Object>> result = streams.stream()
                .map(s -> Map.<String, Object>of(
                        "id", s.getId(),
                        "name", s.getName(),
                        "description", s.getDescription() != null ? s.getDescription() : "",
                        "invite_only", s.getInviteOnly()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/streams/{streamId}/members")
    @Operation(summary = "获取频道成员列表")
    public ResponseEntity<ApiResponse<List<StreamPermissionDTO.MemberInfo>>> getStreamMembers(
            @PathVariable Long streamId) {
        List<UserProfile> members = permissionService.getStreamMembers(streamId);
        
        List<StreamPermissionDTO.MemberInfo> memberInfos = members.stream()
                .limit(50) // 限制返回数量
                .map(u -> StreamPermissionDTO.MemberInfo.builder()
                        .userId(u.getId())
                        .userName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(memberInfos));
    }

    @PostMapping("/streams/{streamId}/members")
    @Operation(summary = "添加频道成员")
    public ResponseEntity<ApiResponse<Void>> addStreamMember(
            @PathVariable Long streamId,
            @RequestBody StreamPermissionDTO.AddMemberRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        if (!permissionService.canManageStream(streamId, userId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("FORBIDDEN", "需要管理员权限"));
        }
        
        permissionService.addStreamMember(streamId, request.getUserId());
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/streams/{streamId}/members/{memberId}")
    @Operation(summary = "移除频道成员")
    public ResponseEntity<ApiResponse<Void>> removeStreamMember(
            @PathVariable Long streamId,
            @PathVariable Long memberId) {
        Long userId = securityUtil.getCurrentUserId();
        
        if (!permissionService.canManageStream(streamId, userId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("FORBIDDEN", "需要管理员权限"));
        }
        
        permissionService.removeStreamMember(streamId, memberId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/streams/{streamId}/subscriber_count")
    @Operation(summary = "获取频道订阅者数量")
    public ResponseEntity<ApiResponse<Long>> getStreamSubscriberCount(@PathVariable Long streamId) {
        Long count = permissionService.getStreamSubscriberCount(streamId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
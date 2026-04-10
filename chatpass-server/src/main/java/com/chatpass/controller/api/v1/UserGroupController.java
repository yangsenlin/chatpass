package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.UserGroupDTO;
import com.chatpass.entity.UserGroup;
import com.chatpass.entity.UserGroupMembership;
import com.chatpass.entity.UserProfile;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.UserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UserGroup 控制器
 * 
 * 用户组管理 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Groups", description = "用户组管理 API")
public class UserGroupController {

    private final UserGroupService groupService;
    private final SecurityUtil securityUtil;

    @PostMapping("/user_groups")
    @Operation(summary = "创建用户组")
    public ResponseEntity<ApiResponse<UserGroupDTO.Response>> createGroup(
            @RequestBody UserGroupDTO.CreateRequest request) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        UserGroup group = groupService.createGroup(realmId, request.getName(), request.getDescription());
        
        return ResponseEntity.ok(ApiResponse.success(toResponse(group)));
    }

    @GetMapping("/user_groups")
    @Operation(summary = "获取 Realm 的所有用户组")
    public ResponseEntity<ApiResponse<List<UserGroupDTO.Response>>> getRealmGroups() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<UserGroup> groups = groupService.getRealmGroups(realmId);
        
        List<UserGroupDTO.Response> responses = groups.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/user_groups/{id}")
    @Operation(summary = "获取用户组详情")
    public ResponseEntity<ApiResponse<UserGroupDTO.DetailResponse>> getGroupDetail(@PathVariable Long id) {
        UserGroup group = groupService.getGroupById(id);
        
        List<UserProfile> members = groupService.getGroupMembers(id);
        Long memberCount = groupService.getGroupMemberCount(id);
        
        List<UserGroupDTO.MemberInfo> memberInfos = members.stream()
                .limit(20) // 最多返回20个成员
                .map(u -> UserGroupDTO.MemberInfo.builder()
                        .userId(u.getId())
                        .userName(u.getFullName())
                        .email(u.getEmail())
                        .build())
                .collect(Collectors.toList());
        
        UserGroupDTO.DetailResponse response = UserGroupDTO.DetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .isSystem(group.getIsSystem())
                .memberCount(memberCount)
                .members(memberInfos)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/user_groups/{id}")
    @Operation(summary = "更新用户组")
    public ResponseEntity<ApiResponse<UserGroupDTO.Response>> updateGroup(
            @PathVariable Long id,
            @RequestBody UserGroupDTO.UpdateRequest request) {
        UserGroup group = groupService.updateGroup(id, request.getName(), request.getDescription());
        
        return ResponseEntity.ok(ApiResponse.success(toResponse(group)));
    }

    @DeleteMapping("/user_groups/{id}")
    @Operation(summary = "删除用户组")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/user_groups/{id}/members")
    @Operation(summary = "添加成员到组")
    public ResponseEntity<ApiResponse<UserGroupDTO.MembershipResponse>> addMember(
            @PathVariable Long id,
            @RequestBody UserGroupDTO.AddMemberRequest request) {
        UserGroupMembership membership = groupService.addMember(id, request.getUserId());
        
        return ResponseEntity.ok(ApiResponse.success(toMembershipResponse(membership)));
    }

    @PostMapping("/user_groups/{id}/members/batch")
    @Operation(summary = "批量添加成员")
    public ResponseEntity<ApiResponse<UserGroupDTO.BatchMembershipResponse>> addMembersBatch(
            @PathVariable Long id,
            @RequestBody UserGroupDTO.BatchAddRequest request) {
        List<UserGroupMembership> memberships = groupService.addMembers(id, request.getUserIds());
        
        List<UserGroupDTO.MembershipResponse> responses = memberships.stream()
                .map(this::toMembershipResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(UserGroupDTO.BatchMembershipResponse.builder()
                .memberships(responses)
                .count(responses.size())
                .build()));
    }

    @DeleteMapping("/user_groups/{groupId}/members/{userId}")
    @Operation(summary = "移除成员")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/user_groups/{id}/members")
    @Operation(summary = "获取组的所有成员")
    public ResponseEntity<ApiResponse<List<UserGroupDTO.MemberInfo>>> getGroupMembers(@PathVariable Long id) {
        List<UserProfile> members = groupService.getGroupMembers(id);
        
        List<UserGroupDTO.MemberInfo> memberInfos = members.stream()
                .map(u -> UserGroupDTO.MemberInfo.builder()
                        .userId(u.getId())
                        .userName(u.getFullName())
                        .email(u.getEmail())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(memberInfos));
    }

    @GetMapping("/users/me/groups")
    @Operation(summary = "获取当前用户所属的组")
    public ResponseEntity<ApiResponse<List<UserGroupDTO.Response>>> getCurrentUserGroups() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<UserGroup> groups = groupService.getUserGroups(userId);
        
        List<UserGroupDTO.Response> responses = groups.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/user_groups/{groupId}/members/{userId}")
    @Operation(summary = "检查用户是否在组中")
    public ResponseEntity<ApiResponse<Boolean>> checkUserInGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        boolean inGroup = groupService.isUserInGroup(groupId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(inGroup));
    }

    @PostMapping("/user_groups/init_system")
    @Operation(summary = "初始化系统组")
    public ResponseEntity<ApiResponse<Map<String, String>>> initSystemGroups() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        groupService.initSystemGroups(realmId);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "系统用户组已初始化")));
    }

    private UserGroupDTO.Response toResponse(UserGroup group) {
        Long memberCount = groupService.getGroupMemberCount(group.getId());
        
        return UserGroupDTO.Response.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .isSystem(group.getIsSystem())
                .memberCount(memberCount)
                .build();
    }

    private UserGroupDTO.MembershipResponse toMembershipResponse(UserGroupMembership membership) {
        return UserGroupDTO.MembershipResponse.builder()
                .id(membership.getId())
                .groupId(membership.getGroup().getId())
                .groupName(membership.getGroup().getName())
                .userId(membership.getUser().getId())
                .userName(membership.getUser().getFullName())
                .joinedAt(membership.getJoinedAt().toString())
                .build();
    }
}
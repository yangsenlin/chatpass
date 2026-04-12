package com.chatpass.controller.api.v1;

import com.chatpass.dto.UserGroupDTO;
import com.chatpass.service.UserGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户组控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserGroupController {
    
    private final UserGroupService groupService;
    
    /**
     * 创建用户组
     */
    @PostMapping("/realm/{realmId}/user_groups")
    public ResponseEntity<UserGroupDTO.GroupInfo> createGroup(
            @PathVariable Long realmId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam Long createdBy) {
        
        UserGroupDTO.GroupInfo group = groupService.createGroup(realmId, name, description, isPublic, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }
    
    /**
     * 获取组织的所有组
     */
    @GetMapping("/realm/{realmId}/user_groups")
    public ResponseEntity<List<UserGroupDTO.GroupInfo>> getRealmGroups(@PathVariable Long realmId) {
        List<UserGroupDTO.GroupInfo> groups = groupService.getGroupsByRealm(realmId);
        return ResponseEntity.ok(groups);
    }
    
    /**
     * 获取公开组
     */
    @GetMapping("/realm/{realmId}/user_groups/public")
    public ResponseEntity<List<UserGroupDTO.GroupInfo>> getPublicGroups(@PathVariable Long realmId) {
        List<UserGroupDTO.GroupInfo> groups = groupService.getPublicGroups(realmId);
        return ResponseEntity.ok(groups);
    }
    
    /**
     * 获取用户所在的组
     */
    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<List<UserGroupDTO.GroupInfo>> getUserGroups(@PathVariable Long userId) {
        List<UserGroupDTO.GroupInfo> groups = groupService.getGroupsByUser(userId);
        return ResponseEntity.ok(groups);
    }
    
    /**
     * 获取组详情
     */
    @GetMapping("/user_groups/{groupId}")
    public ResponseEntity<UserGroupDTO.GroupInfo> getGroup(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "false") boolean includeMembers) {
        
        return groupService.getGroupById(groupId, includeMembers)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新组信息
     */
    @PatchMapping("/user_groups/{groupId}")
    public ResponseEntity<UserGroupDTO.GroupInfo> updateGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic) {
        
        UserGroupDTO.GroupInfo group = groupService.updateGroup(groupId, name, description, isPublic);
        return ResponseEntity.ok(group);
    }
    
    /**
     * 删除组
     */
    @DeleteMapping("/user_groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 添加组成员
     */
    @PostMapping("/user_groups/{groupId}/members")
    public ResponseEntity<UserGroupDTO.MemberInfo> addMember(
            @PathVariable Long groupId,
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "member") String role) {
        
        UserGroupDTO.MemberInfo member = groupService.addMember(groupId, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }
    
    /**
     * 获取组成员列表
     */
    @GetMapping("/user_groups/{groupId}/members")
    public ResponseEntity<List<UserGroupDTO.MemberInfo>> getGroupMembers(@PathVariable Long groupId) {
        List<UserGroupDTO.MemberInfo> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }
    
    /**
     * 移除组成员
     */
    @DeleteMapping("/user_groups/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 更新成员角色
     */
    @PatchMapping("/user_groups/{groupId}/members/{userId}/role")
    public ResponseEntity<UserGroupDTO.MemberInfo> updateMemberRole(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam String role) {
        
        UserGroupDTO.MemberInfo member = groupService.updateMemberRole(groupId, userId, role);
        return ResponseEntity.ok(member);
    }
    
    /**
     * 检查用户是否在组中
     */
    @GetMapping("/user_groups/{groupId}/members/{userId}/check")
    public ResponseEntity<Boolean> checkMembership(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        
        boolean inGroup = groupService.isUserInGroup(groupId, userId);
        return ResponseEntity.ok(inGroup);
    }
}

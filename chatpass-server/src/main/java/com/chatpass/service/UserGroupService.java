package com.chatpass.service;

import com.chatpass.dto.UserGroupDTO;
import com.chatpass.entity.UserGroup;
import com.chatpass.entity.UserGroupMember;
import com.chatpass.repository.UserGroupRepository;
import com.chatpass.repository.UserGroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户组服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserGroupService {
    
    private final UserGroupRepository groupRepository;
    private final UserGroupMemberRepository memberRepository;
    
    /**
     * 创建用户组
     */
    @Transactional
    public UserGroupDTO.GroupInfo createGroup(Long realmId, String name, String description, 
                                                Boolean isPublic, Long createdBy) {
        // 检查名称是否已存在
        if (groupRepository.existsByRealmIdAndName(realmId, name)) {
            throw new IllegalArgumentException("组名称已存在: " + name);
        }
        
        UserGroup group = UserGroup.builder()
                .name(name)
                .description(description)
                .realmId(realmId)
                .isPublic(isPublic != null ? isPublic : true)
                .createdBy(createdBy)
                .build();
        
        group = groupRepository.save(group);
        
        // 创建者自动成为组主人
        UserGroupMember owner = UserGroupMember.builder()
                .groupId(group.getId())
                .userId(createdBy)
                .role("owner")
                .isOwner(true)
                .build();
        
        memberRepository.save(owner);
        
        log.info("创建用户组: {} (realmId: {}, creator: {})", name, realmId, createdBy);
        
        return toGroupInfo(group, 1);
    }
    
    /**
     * 获取组织的所有组
     */
    public List<UserGroupDTO.GroupInfo> getGroupsByRealm(Long realmId) {
        return groupRepository.findByRealmId(realmId)
                .stream()
                .map(g -> toGroupInfo(g, (int) memberRepository.countByGroupId(g.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取公开组
     */
    public List<UserGroupDTO.GroupInfo> getPublicGroups(Long realmId) {
        return groupRepository.findByRealmIdAndIsPublicTrue(realmId)
                .stream()
                .map(g -> toGroupInfo(g, (int) memberRepository.countByGroupId(g.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户所在的组
     */
    public List<UserGroupDTO.GroupInfo> getGroupsByUser(Long userId) {
        return groupRepository.findGroupsByUserId(userId)
                .stream()
                .map(g -> toGroupInfo(g, (int) memberRepository.countByGroupId(g.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取组详情
     */
    public Optional<UserGroupDTO.GroupInfo> getGroupById(Long groupId, boolean includeMembers) {
        return groupRepository.findById(groupId)
                .map(g -> {
                    int memberCount = (int) memberRepository.countByGroupId(g.getId());
                    UserGroupDTO.GroupInfo info = toGroupInfo(g, memberCount);
                    
                    if (includeMembers) {
                        List<UserGroupDTO.MemberInfo> members = memberRepository.findByGroupId(groupId)
                                .stream()
                                .map(this::toMemberInfo)
                                .collect(Collectors.toList());
                        info.setMembers(members);
                    }
                    
                    return info;
                });
    }
    
    /**
     * 更新组信息
     */
    @Transactional
    public UserGroupDTO.GroupInfo updateGroup(Long groupId, String name, String description, Boolean isPublic) {
        UserGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("组不存在: " + groupId));
        
        if (name != null && !name.equals(group.getName())) {
            if (groupRepository.existsByRealmIdAndName(group.getRealmId(), name)) {
                throw new IllegalArgumentException("组名称已存在: " + name);
            }
            group.setName(name);
        }
        
        if (description != null) {
            group.setDescription(description);
        }
        
        if (isPublic != null) {
            group.setIsPublic(isPublic);
        }
        
        group = groupRepository.save(group);
        log.info("更新用户组: {}", group.getName());
        
        return toGroupInfo(group, (int) memberRepository.countByGroupId(groupId));
    }
    
    /**
     * 添加组成员
     */
    @Transactional
    public UserGroupDTO.MemberInfo addMember(Long groupId, Long userId, String role) {
        // 检查组是否存在
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("组不存在: " + groupId);
        }
        
        // 检查用户是否已在组中
        if (memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("用户已在组中");
        }
        
        UserGroupMember member = UserGroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(role != null ? role : "member")
                .isOwner("owner".equals(role))
                .build();
        
        member = memberRepository.save(member);
        log.info("添加组成员: userId={}, groupId={}, role={}", userId, groupId, role);
        
        return toMemberInfo(member);
    }
    
    /**
     * 移除组成员
     */
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        UserGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在"));
        
        if (member.getIsOwner()) {
            // 检查是否还有其他主人
            long ownerCount = memberRepository.findByGroupIdAndIsOwnerTrue(groupId).size();
            if (ownerCount <= 1) {
                throw new IllegalStateException("不能移除最后一个组主人");
            }
        }
        
        memberRepository.deleteByGroupIdAndUserId(groupId, userId);
        log.info("移除组成员: userId={}, groupId={}", userId, groupId);
    }
    
    /**
     * 更新成员角色
     */
    @Transactional
    public UserGroupDTO.MemberInfo updateMemberRole(Long groupId, Long userId, String newRole) {
        UserGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在"));
        
        // 如果降级主人，检查是否还有其他主人
        if (member.getIsOwner() && !"owner".equals(newRole)) {
            long ownerCount = memberRepository.findByGroupIdAndIsOwnerTrue(groupId).size();
            if (ownerCount <= 1) {
                throw new IllegalStateException("不能降级最后一个组主人");
            }
        }
        
        member.setRole(newRole);
        member.setIsOwner("owner".equals(newRole));
        
        member = memberRepository.save(member);
        log.info("更新成员角色: userId={}, groupId={}, role={}", userId, groupId, newRole);
        
        return toMemberInfo(member);
    }
    
    /**
     * 删除组
     */
    @Transactional
    public void deleteGroup(Long groupId) {
        UserGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("组不存在: " + groupId));
        
        // 删除所有成员
        memberRepository.findByGroupId(groupId).forEach(m -> memberRepository.delete(m));
        
        // 删除组
        groupRepository.delete(group);
        log.info("删除用户组: {}", group.getName());
    }
    
    /**
     * 获取组成员列表
     */
    public List<UserGroupDTO.MemberInfo> getGroupMembers(Long groupId) {
        return memberRepository.findByGroupId(groupId)
                .stream()
                .map(this::toMemberInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否在组中
     */
    public boolean isUserInGroup(Long groupId, Long userId) {
        return memberRepository.existsByGroupIdAndUserId(groupId, userId);
    }
    
    private UserGroupDTO.GroupInfo toGroupInfo(UserGroup group, int memberCount) {
        return UserGroupDTO.GroupInfo.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .realmId(group.getRealmId())
                .isPublic(group.getIsPublic())
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .memberCount(memberCount)
                .build();
    }
    
    private UserGroupDTO.MemberInfo toMemberInfo(UserGroupMember member) {
        return UserGroupDTO.MemberInfo.builder()
                .id(member.getId())
                .groupId(member.getGroupId())
                .userId(member.getUserId())
                .role(member.getRole())
                .isOwner(member.getIsOwner())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}

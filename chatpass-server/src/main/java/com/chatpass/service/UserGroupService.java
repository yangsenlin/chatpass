package com.chatpass.service;

import com.chatpass.entity.Realm;
import com.chatpass.entity.UserGroup;
import com.chatpass.entity.UserGroupMembership;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserGroupMembershipRepository;
import com.chatpass.repository.UserGroupRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserGroupService
 * 
 * 用户组管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserGroupService {

    private final UserGroupRepository groupRepository;
    private final UserGroupMembershipRepository membershipRepository;
    private final RealmRepository realmRepository;
    private final UserProfileRepository userRepository;

    /**
     * 创建用户组
     */
    @Transactional
    public UserGroup createGroup(Long realmId, String name, String description) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm 不存在"));
        
        if (groupRepository.existsByRealmIdAndName(realmId, name)) {
            throw new IllegalArgumentException("用户组已存在: " + name);
        }
        
        UserGroup group = UserGroup.builder()
                .realm(realm)
                .name(name)
                .description(description)
                .isSystem(false)
                .build();
        
        groupRepository.save(group);
        log.info("Created user group: {} in realm {}", name, realmId);
        
        return group;
    }

    /**
     * 更新用户组
     */
    @Transactional
    public UserGroup updateGroup(Long groupId, String name, String description) {
        UserGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("用户组不存在"));
        
        if (group.getIsSystem()) {
            throw new IllegalArgumentException("系统组不能修改");
        }
        
        if (name != null && !name.equals(group.getName())) {
            if (groupRepository.existsByRealmIdAndName(group.getRealm().getId(), name)) {
                throw new IllegalArgumentException("用户组名称已存在: " + name);
            }
            group.setName(name);
        }
        
        if (description != null) {
            group.setDescription(description);
        }
        
        groupRepository.save(group);
        log.info("Updated user group: {}", groupId);
        
        return group;
    }

    /**
     * 删除用户组
     */
    @Transactional
    public void deleteGroup(Long groupId) {
        UserGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("用户组不存在"));
        
        if (group.getIsSystem()) {
            throw new IllegalArgumentException("系统组不能删除");
        }
        
        // 删除所有成员关系
        membershipRepository.deleteByGroupId(groupId);
        
        // 删除组
        groupRepository.delete(group);
        log.info("Deleted user group: {}", groupId);
    }

    /**
     * 添加用户到组
     */
    @Transactional
    public UserGroupMembership addMember(Long groupId, Long userId) {
        UserGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("用户组不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        if (membershipRepository.existsByGroupAndUser(group, user)) {
            log.debug("User {} already in group {}", userId, groupId);
            return membershipRepository.findByGroupAndUser(group, user).orElse(null);
        }
        
        UserGroupMembership membership = UserGroupMembership.builder()
                .group(group)
                .user(user)
                .build();
        
        membershipRepository.save(membership);
        log.info("Added user {} to group {}", userId, groupId);
        
        return membership;
    }

    /**
     * 批量添加用户到组
     */
    @Transactional
    public List<UserGroupMembership> addMembers(Long groupId, List<Long> userIds) {
        return userIds.stream()
                .map(userId -> addMember(groupId, userId))
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    /**
     * 移除用户从组
     */
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        UserGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("用户组不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        membershipRepository.deleteByGroupAndUser(group, user);
        log.info("Removed user {} from group {}", userId, groupId);
    }

    /**
     * 获取 Realm 的所有用户组
     */
    public List<UserGroup> getRealmGroups(Long realmId) {
        return groupRepository.findByRealmIdOrderByName(realmId);
    }

    /**
     * 获取组的所有成员
     */
    public List<UserProfile> getGroupMembers(Long groupId) {
        return membershipRepository.findUsersByGroupId(groupId);
    }

    /**
     * 获取用户所属的所有组
     */
    public List<UserGroup> getUserGroups(Long userId) {
        return membershipRepository.findGroupsByUserId(userId);
    }

    /**
     * 获取组的成员数量
     */
    public Long getGroupMemberCount(Long groupId) {
        return membershipRepository.countByGroupId(groupId);
    }

    /**
     * 检查用户是否在组中
     */
    public boolean isUserInGroup(Long groupId, Long userId) {
        UserGroup group = groupRepository.findById(groupId).orElse(null);
        UserProfile user = userRepository.findById(userId).orElse(null);
        
        if (group == null || user == null) return false;
        
        return membershipRepository.existsByGroupAndUser(group, user);
    }

    /**
     * 获取用户组详情
     */
    public UserGroup getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("用户组不存在"));
    }

    /**
     * 初始化系统用户组
     */
    @Transactional
    public void initSystemGroups(Long realmId) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm 不存在"));
        
        // 创建系统组
        createSystemGroup(realm, UserGroup.SYSTEM_ADMIN, "管理员组");
        createSystemGroup(realm, UserGroup.SYSTEM_MODERATOR, "版主组");
        createSystemGroup(realm, UserGroup.SYSTEM_MEMBERS, "成员组");
        
        log.info("Initialized system groups for realm {}", realmId);
    }

    private void createSystemGroup(Realm realm, String name, String description) {
        if (!groupRepository.existsByRealmIdAndName(realm.getId(), name)) {
            UserGroup group = UserGroup.builder()
                    .realm(realm)
                    .name(name)
                    .description(description)
                    .isSystem(true)
                    .build();
            
            groupRepository.save(group);
        }
    }
}
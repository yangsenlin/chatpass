package com.chatpass.service;

import com.chatpass.dto.GroupGroupMembershipDTO;
import com.chatpass.entity.GroupGroupMembership;
import com.chatpass.entity.UserGroup;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.GroupGroupMembershipRepository;
import com.chatpass.repository.UserGroupMembershipRepository;
import com.chatpass.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GroupGroupMembershipService
 * 
 * 用户组嵌套关系管理服务
 * 
 * 核心功能：
 * - 管理用户组的嵌套关系（父组包含子组）
 * - 提供层级查询（祖先、子孙）
 * - 防止循环引用
 * - 支持权限继承计算
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupGroupMembershipService {

    private final GroupGroupMembershipRepository membershipRepository;
    private final UserGroupRepository groupRepository;
    private final UserGroupMembershipRepository userMembershipRepository;

    private static final String CACHE_NAME = "group_hierarchy";

    /**
     * 添加子组到父组
     * 
     * @param supergroupId 父组ID
     * @param subgroupId 子组ID
     * @return 嵌套关系
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public GroupGroupMembershipDTO.Response addSubgroup(Long supergroupId, Long subgroupId) {
        // 验证父组存在
        UserGroup supergroup = groupRepository.findById(supergroupId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup", supergroupId));

        // 验证子组存在
        UserGroup subgroup = groupRepository.findById(subgroupId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup", subgroupId));

        // 不能添加自己到自己
        if (supergroupId.equals(subgroupId)) {
            throw new IllegalArgumentException("组不能包含自己");
        }

        // 检查是否会造成循环引用
        if (membershipRepository.wouldCreateCycle(supergroupId, subgroupId)) {
            throw new IllegalArgumentException(
                    String.format("添加此关系会造成循环引用：组 %d 已经是组 %d 的祖先", subgroupId, supergroupId));
        }

        // 检查关系是否已存在
        if (membershipRepository.existsBySupergroupIdAndSubgroupId(supergroupId, subgroupId)) {
            log.debug("嵌套关系已存在: 父组 {} 子组 {}", supergroupId, subgroupId);
            return findBySupergroupIdAndSubgroupId(supergroupId, subgroupId);
        }

        // 创建嵌套关系
        GroupGroupMembership membership = GroupGroupMembership.builder()
                .supergroup(supergroup)
                .subgroup(subgroup)
                .build();

        membership = membershipRepository.save(membership);
        log.info("添加嵌套关系: 父组 {} -> 子组 {}", supergroupId, subgroupId);

        return toResponse(membership);
    }

    /**
     * 批量添加子组
     * 
     * @param supergroupId 父组ID
     * @param subgroupIds 子组ID列表
     * @return 添加结果列表
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public List<GroupGroupMembershipDTO.Response> addSubgroups(Long supergroupId, List<Long> subgroupIds) {
        return subgroupIds.stream()
                .map(subgroupId -> {
                    try {
                        return addSubgroup(supergroupId, subgroupId);
                    } catch (Exception e) {
                        log.warn("添加子组 {} 到父组 {} 失败: {}", subgroupId, supergroupId, e.getMessage());
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    /**
     * 移除子组
     * 
     * @param supergroupId 父组ID
     * @param subgroupId 子组ID
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void removeSubgroup(Long supergroupId, Long subgroupId) {
        membershipRepository.deleteBySupergroupIdAndSubgroupId(supergroupId, subgroupId);
        log.info("移除嵌套关系: 父组 {} -> 子组 {}", supergroupId, subgroupId);
    }

    /**
     * 获取父组的所有直接子组
     * 
     * @param supergroupId 父组ID
     * @return 子组列表
     */
    @Transactional(readOnly = true)
    public GroupGroupMembershipDTO.ListResponse getSubgroups(Long supergroupId) {
        List<GroupGroupMembership> memberships = membershipRepository.findBySupergroupId(supergroupId);

        List<GroupGroupMembershipDTO.Response> responses = memberships.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return GroupGroupMembershipDTO.ListResponse.builder()
                .subgroups(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取子组的所有直接父组
     * 
     * @param subgroupId 子组ID
     * @return 父组列表
     */
    @Transactional(readOnly = true)
    public GroupGroupMembershipDTO.SupergroupsResponse getSupergroups(Long subgroupId) {
        List<GroupGroupMembership> memberships = membershipRepository.findBySubgroupId(subgroupId);

        List<GroupGroupMembershipDTO.Response> responses = memberships.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return GroupGroupMembershipDTO.SupergroupsResponse.builder()
                .supergroups(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取组的层级信息（包含所有祖先和子孙）
     * 
     * @param groupId 组ID
     * @return 层级信息
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "#groupId")
    public GroupGroupMembershipDTO.HierarchyResponse getHierarchyInfo(Long groupId) {
        // 验证组存在
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup", groupId));

        // 获取所有子孙组
        List<Long> descendantIds = membershipRepository.findAllDescendantGroupIds(groupId);

        // 获取所有祖先组
        List<Long> ancestorIds = membershipRepository.findAllAncestorGroupIds(groupId);

        // 统计直接关系
        Long directSubgroupCount = membershipRepository.countSubgroupsBySupergroupId(groupId);
        Long directSupergroupCount = membershipRepository.countSupergroupsBySubgroupId(groupId);

        UserGroup group = groupRepository.findById(groupId).orElse(null);

        return GroupGroupMembershipDTO.HierarchyResponse.builder()
                .groupId(groupId)
                .groupName(group != null ? group.getName() : null)
                .directSubgroupCount(directSubgroupCount.intValue())
                .allDescendantIds(descendantIds)
                .directSupergroupCount(directSupergroupCount.intValue())
                .allAncestorIds(ancestorIds)
                .build();
    }

    /**
     * 检查是否是祖先组（递归）
     * 判断 ancestorGroup 是否是 descendantGroup 的祖先
     * 
     * @param ancestorGroupId 祖先组ID
     * @param descendantGroupId 后代组ID
     * @return 是否是祖先关系
     */
    @Transactional(readOnly = true)
    public boolean isAncestorOf(Long ancestorGroupId, Long descendantGroupId) {
        List<Long> ancestorIds = membershipRepository.findAllAncestorGroupIds(descendantGroupId);
        return ancestorIds.contains(ancestorGroupId);
    }

    /**
     * 检查是否是后代组（递归）
     * 判断 descendantGroup 是否是 ancestorGroup 的后代
     * 
     * @param descendantGroupId 后代组ID
     * @param ancestorGroupId 祖先组ID
     * @return 是否是后代关系
     */
    @Transactional(readOnly = true)
    public boolean isDescendantOf(Long descendantGroupId, Long ancestorGroupId) {
        return isAncestorOf(ancestorGroupId, descendantGroupId);
    }

    /**
     * 获取组的所有成员（包括子组的成员，递归）
     * 
     * @param groupId 组ID
     * @return 所有成员ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getAllMemberIds(Long groupId) {
        // 获取所有子孙组（包括自己）
        List<Long> groupIds = membershipRepository.findAllDescendantGroupIds(groupId);

        // 获取所有组的成员
        return groupIds.stream()
                .flatMap(gid -> userMembershipRepository.findUserIdsByGroupId(gid).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 检查嵌套关系是否存在
     * 
     * @param supergroupId 父组ID
     * @param subgroupId 子组ID
     * @return 是否存在嵌套关系
     */
    @Transactional(readOnly = true)
    public boolean existsMembership(Long supergroupId, Long subgroupId) {
        return membershipRepository.existsBySupergroupIdAndSubgroupId(supergroupId, subgroupId);
    }

    /**
     * 删除组时清理所有相关嵌套关系
     * 
     * @param groupId 组ID
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void clearAllMemberships(Long groupId) {
        membershipRepository.deleteAllByGroupId(groupId);
        log.info("清理组 {} 的所有嵌套关系", groupId);
    }

    /**
     * 根据ID查询嵌套关系
     */
    @Transactional(readOnly = true)
    public GroupGroupMembershipDTO.Response findById(Long id) {
        return membershipRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("GroupGroupMembership", id));
    }

    /**
     * 根据父组和子组查询嵌套关系
     */
    @Transactional(readOnly = true)
    public GroupGroupMembershipDTO.Response findBySupergroupIdAndSubgroupId(Long supergroupId, Long subgroupId) {
        return membershipRepository.findBySupergroupIdAndSubgroupId(supergroupId, subgroupId)
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     * 转换为响应DTO
     */
    private GroupGroupMembershipDTO.Response toResponse(GroupGroupMembership membership) {
        UserGroup supergroup = membership.getSupergroup();
        UserGroup subgroup = membership.getSubgroup();

        // 获取成员数量
        Long supergroupMemberCount = userMembershipRepository.countByGroupId(supergroup.getId());
        Long subgroupMemberCount = userMembershipRepository.countByGroupId(subgroup.getId());

        return GroupGroupMembershipDTO.Response.builder()
                .id(membership.getId())
                .supergroup(GroupGroupMembershipDTO.GroupInfo.builder()
                        .id(supergroup.getId())
                        .name(supergroup.getName())
                        .description(supergroup.getDescription())
                        .isSystem(supergroup.getIsSystem())
                        .memberCount(supergroupMemberCount)
                        .build())
                .subgroup(GroupGroupMembershipDTO.GroupInfo.builder()
                        .id(subgroup.getId())
                        .name(subgroup.getName())
                        .description(subgroup.getDescription())
                        .isSystem(subgroup.getIsSystem())
                        .memberCount(subgroupMemberCount)
                        .build())
                .createdAt(membership.getCreatedAt())
                .build();
    }
}
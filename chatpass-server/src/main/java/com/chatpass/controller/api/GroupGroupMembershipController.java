package com.chatpass.controller.api;

import com.chatpass.dto.GroupGroupMembershipDTO;
import com.chatpass.service.GroupGroupMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GroupGroupMembershipController
 * 
 * 用户组嵌套关系管理 API
 * 
 * 功能：
 * - 添加/移除子组
 * - 查询组的层级关系
 * - 获取组的所有成员（递归）
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Group Group Membership", description = "用户组嵌套关系管理")
public class GroupGroupMembershipController {

    private final GroupGroupMembershipService membershipService;

    /**
     * 添加子组到父组
     * 
     * POST /api/v1/groups/{supergroupId}/subgroups
     */
    @PostMapping("/{supergroupId}/subgroups")
    @Operation(summary = "添加子组", description = "将子组添加到父组，形成嵌套关系")
    public ResponseEntity<GroupGroupMembershipDTO.Response> addSubgroup(
            @Parameter(description = "父组ID") @PathVariable Long supergroupId,
            @Valid @RequestBody GroupGroupMembershipDTO.CreateRequest request) {

        log.info("添加子组 {} 到父组 {}", request.getSubgroupId(), supergroupId);

        GroupGroupMembershipDTO.Response response = membershipService.addSubgroup(
                supergroupId, request.getSubgroupId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 批量添加子组
     * 
     * POST /api/v1/groups/{supergroupId}/subgroups/batch
     */
    @PostMapping("/{supergroupId}/subgroups/batch")
    @Operation(summary = "批量添加子组", description = "批量添加多个子组到父组")
    public ResponseEntity<GroupGroupMembershipDTO.ListResponse> addSubgroups(
            @Parameter(description = "父组ID") @PathVariable Long supergroupId,
            @Valid @RequestBody GroupGroupMembershipDTO.BatchAddRequest request) {

        log.info("批量添加 {} 个子组到父组 {}", 
                request.getSubgroupIds().size(), supergroupId);

        var responses = membershipService.addSubgroups(supergroupId, request.getSubgroupIds());

        GroupGroupMembershipDTO.ListResponse response = GroupGroupMembershipDTO.ListResponse.builder()
                .subgroups(responses)
                .count(responses.size())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 移除子组
     * 
     * DELETE /api/v1/groups/{supergroupId}/subgroups/{subgroupId}
     */
    @DeleteMapping("/{supergroupId}/subgroups/{subgroupId}")
    @Operation(summary = "移除子组", description = "从父组中移除子组")
    public ResponseEntity<Void> removeSubgroup(
            @Parameter(description = "父组ID") @PathVariable Long supergroupId,
            @Parameter(description = "子组ID") @PathVariable Long subgroupId) {

        log.info("从父组 {} 移除子组 {}", supergroupId, subgroupId);

        membershipService.removeSubgroup(supergroupId, subgroupId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 获取父组的所有直接子组
     * 
     * GET /api/v1/groups/{supergroupId}/subgroups
     */
    @GetMapping("/{supergroupId}/subgroups")
    @Operation(summary = "获取子组列表", description = "获取父组的所有直接子组")
    public ResponseEntity<GroupGroupMembershipDTO.ListResponse> getSubgroups(
            @Parameter(description = "父组ID") @PathVariable Long supergroupId) {

        GroupGroupMembershipDTO.ListResponse response = membershipService.getSubgroups(supergroupId);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取子组的所有直接父组
     * 
     * GET /api/v1/groups/{subgroupId}/supergroups
     */
    @GetMapping("/{subgroupId}/supergroups")
    @Operation(summary = "获取父组列表", description = "获取子组的所有直接父组")
    public ResponseEntity<GroupGroupMembershipDTO.SupergroupsResponse> getSupergroups(
            @Parameter(description = "子组ID") @PathVariable Long subgroupId) {

        GroupGroupMembershipDTO.SupergroupsResponse response = membershipService.getSupergroups(subgroupId);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取组的层级信息
     * 
     * GET /api/v1/groups/{groupId}/hierarchy
     */
    @GetMapping("/{groupId}/hierarchy")
    @Operation(summary = "获取层级信息", description = "获取组的完整层级信息（祖先和子孙）")
    public ResponseEntity<GroupGroupMembershipDTO.HierarchyResponse> getHierarchy(
            @Parameter(description = "组ID") @PathVariable Long groupId) {

        GroupGroupMembershipDTO.HierarchyResponse response = membershipService.getHierarchyInfo(groupId);

        return ResponseEntity.ok(response);
    }

    /**
     * 检查是否是祖先组
     * 
     * GET /api/v1/groups/{ancestorGroupId}/is-ancestor-of/{descendantGroupId}
     */
    @GetMapping("/{ancestorGroupId}/is-ancestor-of/{descendantGroupId}")
    @Operation(summary = "检查祖先关系", description = "检查组是否是另一个组的祖先")
    public ResponseEntity<Boolean> isAncestorOf(
            @Parameter(description = "祖先组ID") @PathVariable Long ancestorGroupId,
            @Parameter(description = "后代组ID") @PathVariable Long descendantGroupId) {

        boolean result = membershipService.isAncestorOf(ancestorGroupId, descendantGroupId);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取组的所有成员（递归，包括子组成员）
     * 
     * GET /api/v1/groups/{groupId}/all-members
     */
    @GetMapping("/{groupId}/all-members")
    @Operation(summary = "获取所有成员", description = "递归获取组的所有成员（包括子组的成员）")
    public ResponseEntity<List<Long>> getAllMembers(
            @Parameter(description = "组ID") @PathVariable Long groupId) {

        List<Long> memberIds = membershipService.getAllMemberIds(groupId);

        return ResponseEntity.ok(memberIds);
    }

    /**
     * 检查嵌套关系是否存在
     * 
     * GET /api/v1/groups/{supergroupId}/has-subgroup/{subgroupId}
     */
    @GetMapping("/{supergroupId}/has-subgroup/{subgroupId}")
    @Operation(summary = "检查嵌套关系", description = "检查父组是否包含子组")
    public ResponseEntity<Boolean> hasSubgroup(
            @Parameter(description = "父组ID") @PathVariable Long supergroupId,
            @Parameter(description = "子组ID") @PathVariable Long subgroupId) {

        boolean result = membershipService.existsMembership(supergroupId, subgroupId);

        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID获取嵌套关系详情
     * 
     * GET /api/v1/groups/memberships/{membershipId}
     */
    @GetMapping("/memberships/{membershipId}")
    @Operation(summary = "获取嵌套关系详情", description = "根据ID获取嵌套关系详情")
    public ResponseEntity<GroupGroupMembershipDTO.Response> getMembership(
            @Parameter(description = "嵌套关系ID") @PathVariable Long membershipId) {

        GroupGroupMembershipDTO.Response response = membershipService.findById(membershipId);

        return ResponseEntity.ok(response);
    }
}
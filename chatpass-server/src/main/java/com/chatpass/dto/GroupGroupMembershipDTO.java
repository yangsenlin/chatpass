package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GroupGroupMembership DTO
 * 
 * 用户组嵌套关系数据传输对象
 */
public class GroupGroupMembershipDTO {

    /**
     * 创建嵌套关系请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        /**
         * 子组ID
         */
        private Long subgroupId;
    }

    /**
     * 批量添加子组请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchAddRequest {
        /**
         * 子组ID列表
         */
        private List<Long> subgroupIds;
    }

    /**
     * 嵌套关系响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        /**
         * 关系ID
         */
        private Long id;

        /**
         * 父组信息
         */
        private GroupInfo supergroup;

        /**
         * 子组信息
         */
        private GroupInfo subgroup;

        /**
         * 创建时间
         */
        private LocalDateTime createdAt;
    }

    /**
     * 组简要信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo {
        private Long id;
        private String name;
        private String description;
        private Boolean isSystem;
        private Long memberCount;
    }

    /**
     * 列表响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        /**
         * 子组列表（父组视角）
         */
        private List<Response> subgroups;

        /**
         * 总数
         */
        private Integer count;
    }

    /**
     * 父组列表响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupergroupsResponse {
        /**
         * 父组列表（子组视角）
         */
        private List<Response> supergroups;

        /**
         * 总数
         */
        private Integer count;
    }

    /**
     * 层级信息响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyResponse {
        /**
         * 组ID
         */
        private Long groupId;

        /**
         * 组名称
         */
        private String groupName;

        /**
         * 直接子组数量
         */
        private Integer directSubgroupCount;

        /**
         * 所有子孙组ID（递归）
         */
        private List<Long> allDescendantIds;

        /**
         * 直接父组数量
         */
        private Integer directSupergroupCount;

        /**
         * 所有祖先组ID（递归）
         */
        private List<Long> allAncestorIds;
    }
}
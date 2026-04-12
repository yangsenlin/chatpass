package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户组DTO
 */
public class UserGroupDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupInfo {
        private Long id;
        private String name;
        private String description;
        private Long realmId;
        private Boolean isPublic;
        private Long createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        /**
         * 成员数量
         */
        private Integer memberCount;
        
        /**
         * 成员列表（可选）
         */
        private List<MemberInfo> members;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private Long id;
        private Long groupId;
        private Long userId;
        private String role;
        private Boolean isOwner;
        private LocalDateTime joinedAt;
        
        /**
         * 用户名（可选）
         */
        private String userName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String name;
        private String description;
        private Boolean isPublic;
        private Long createdBy;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private Boolean isPublic;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddMemberRequest {
        private Long userId;
        private String role;
    }
}

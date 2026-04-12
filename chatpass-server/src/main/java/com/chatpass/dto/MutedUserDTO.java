package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MutedUser DTO
 * 
 * 用户屏蔽相关数据传输对象
 */
public class MutedUserDTO {

    /**
     * 屏蔽请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MuteRequest {
        private Long mutedUserId;
    }

    /**
     * 屏蔽响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long mutedUserId;
        private String mutedUserEmail;
        private String mutedUserFullName;
        private String mutedUserAvatarUrl;
        private LocalDateTime dateMuted;
    }

    /**
     * 屏蔽列表响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private List<Response> mutedUsers;
        private Integer count;
    }

    /**
     * 简化的屏蔽用户ID列表
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IdListResponse {
        private List<Long> mutedUserIds;
        private Integer count;
    }
}
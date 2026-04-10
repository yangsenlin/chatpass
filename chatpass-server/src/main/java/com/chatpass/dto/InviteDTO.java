package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Invite DTO
 */
public class InviteDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String email;
        private Integer maxUses = 1;
        private Integer expireDays = 7;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcceptRequest {
        private String inviteLink;
        private String email;
        private String fullName;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String inviteLink;
        private String email;
        private Integer status;
        private String statusText;
        private Integer maxUses;
        private Integer currentUses;
        private String expiresAt;
        private String createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private java.util.List<Response> invites;
        private Integer count;
    }
}
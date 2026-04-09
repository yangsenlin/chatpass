package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户 DTO
 */
public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String fullName;
        private String timezone;
        private String defaultLanguage;
        private Boolean enableDesktopNotifications;
        private Boolean enableSounds;
        private Boolean twentyFourHourTime;
        private Integer colorScheme;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long realmId;
        private String email;
        private String fullName;
        private String shortName;
        private String avatarUrl;
        private String timezone;
        private String defaultLanguage;
        private Integer role;
        private Boolean isActive;
        private Boolean isGuest;
        private Boolean isBot;
        private LocalDateTime dateJoined;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private java.util.List<Response> members;
        private Long total;
    }
}
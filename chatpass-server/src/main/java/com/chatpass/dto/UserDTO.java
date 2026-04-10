package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户 DTO
 */
public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String email;
        private String fullName;
        private String shortName;
        private String avatarUrl;
        private Integer role;
        private String timezone;
        private Boolean isActive;
        private Boolean isBot;
        private LocalDateTime dateJoined;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String fullName;
        private String shortName;
        private String timezone;
        private String defaultLanguage;
        private Boolean enableDesktopNotifications;
        private Boolean enableSounds;
        private Boolean enterSends;
        private Boolean twentyFourHourTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileResponse {
        private Long id;
        private String email;
        private String fullName;
        private String shortName;
        private String avatarUrl;
        private Integer role;
        private String timezone;
        private String defaultLanguage;
        private Boolean isActive;
        private Boolean isBot;
        private Boolean isBillingAdmin;
        private LocalDateTime dateJoined;
        private LocalDateTime lastLogin;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private java.util.List<Response> users;
        private Long total;
    }
}
package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Reaction DTO
 */
public class ReactionDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddRequest {
        private Long messageId;
        private String emojiCode;
        private String emojiName;
        private String emojiType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long messageId;
        private Long userId;
        private String userName;
        private String emojiCode;
        private String emojiName;
        private String emojiType;
        private LocalDateTime dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AggregatedResponse {
        private String emojiCode;
        private String emojiName;
        private Integer count;
        private List<UserInfo> users;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private Long messageId;
        private List<AggregatedResponse> reactions;
    }
}
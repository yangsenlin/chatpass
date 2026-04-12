package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机器人DTO
 */
public class BotDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BotInfo {
        private Long id;
        private Long userId;
        private String name;
        private String botType;
        private String apiKey;
        private Long realmId;
        private Long ownerId;
        private String avatarUrl;
        private String description;
        private String webhookUrl;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String name;
        private String botType;
        private String description;
        private String webhookUrl;
        private Long ownerId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private String avatarUrl;
        private String webhookUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private Long streamId;
        private String topic;
        private String content;
        private List<Long> toUserIds;
    }
}

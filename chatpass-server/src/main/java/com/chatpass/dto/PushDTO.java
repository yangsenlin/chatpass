package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PushDTO
 */
public class PushDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PushConfigResponse {
        private Long id;
        private Long realmId;
        private String pushType;
        private String projectId;
        private String senderId;
        private Boolean isActive;
        private Integer batchSize;
        private String dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePushConfigRequest {
        private String pushType;
        private String projectId;
        private String apiKey;
        private String senderId;
        private String certificatePath;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendPushRequest {
        private Long pushConfigId;
        private Long userId;
        private Long messageId;
        private String deviceToken;
        private String notificationType;
        private String title;
        private String body;
        private String dataPayload;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationResponse {
        private Long id;
        private Long pushConfigId;
        private Long userId;
        private Long messageId;
        private String deviceToken;
        private String notificationType;
        private String title;
        private String body;
        private String status;
        private String errorMessage;
        private String dateCreated;
        private String dateSent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PushStats {
        private Long pendingCount;
        private Long sentCount;
        private Long failedCount;
    }
}
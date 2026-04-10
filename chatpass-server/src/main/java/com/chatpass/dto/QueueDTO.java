package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * QueueDTO
 */
public class QueueDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueResponse {
        private Long id;
        private Long realmId;
        private String queueName;
        private String queueType;
        private String brokerUrl;
        private String exchangeName;
        private String routingKey;
        private Boolean isActive;
        private Integer maxRetry;
        private Integer retryDelaySeconds;
        private String dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateQueueRequest {
        private String queueName;
        private String queueType;
        private String brokerUrl;
        private String exchangeName;
        private String routingKey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnqueueRequest {
        private Long queueId;
        private Long messageId;
        private String payload;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueuedMessageResponse {
        private Long id;
        private Long queueId;
        private Long messageId;
        private String payload;
        private String status;
        private Integer retryCount;
        private String errorMessage;
        private String dateQueued;
        private String dateSent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueStats {
        private Long queueId;
        private String queueName;
        private Long pendingCount;
        private Long sentCount;
        private Long failedCount;
    }
}
package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WebhookDTO
 * 
 * Webhook 数据传输对象
 */
public class WebhookDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebhookResponse {
        private Long id;
        private String name;
        private String webhookKey;
        private String webhookUrl;
        private Long ownerId;
        private Long realmId;
        private Long botId;
        private Long targetStreamId;
        private String defaultTopic;
        private String eventTypes;
        private String description;
        private Boolean isActive;
        private Long invokeCount;
        private Long successCount;
        private Long failureCount;
        private Double successRate;
        private Double avgResponseTime;
        private String lastInvoked;
        private String lastResult;
        private String dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateWebhookRequest {
        private String name;
        private String webhookUrl;
        private Long targetStreamId;
        private String defaultTopic;
        private String eventTypes;        // JSON 数组，如 ["message.created", "reaction.added"]
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateWebhookRequest {
        private String name;
        private String webhookUrl;
        private String eventTypes;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebhookKeyResponse {
        private Long webhookId;
        private String webhookKey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebhookLogResponse {
        private Long id;
        private Long webhookId;
        private String webhookName;
        private String eventType;
        private String eventData;
        private String requestUrl;
        private String requestMethod;
        private Integer responseStatus;
        private String responseBody;
        private String result;
        private String errorMessage;
        private Integer retryAttempt;
        private String invokeTime;
        private Long responseTimeMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebhookSummary {
        private Long webhookId;
        private String webhookName;
        private Long totalInvocations;
        private Long successCount;
        private Long failureCount;
        private Double successRate;
        private Double avgResponseTime;
        private String lastInvoked;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebhookPayload {
        private String webhookKey;
        private String eventType;
        private Object data;
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvokeRequest {
        private String eventType;
        private Object data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvokeResponse {
        private String webhookKey;
        private String eventType;
        private Boolean invoked;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestResponse {
        private Long webhookId;
        private Boolean success;
        private Integer responseStatus;
        private Long responseTimeMs;
        private String errorMessage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebhookListResponse {
        private List<WebhookResponse> webhooks;
        private Long total;
    }
}
package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TopicDTO
 */
public class TopicDTO {

    // Topic Management DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicResponse {
        private Long streamId;
        private String topicName;
        private Long messageCount;
        private Long lastMessageId;
        private String lastMessageTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RenameRequest {
        private String oldTopic;
        private String newTopic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RenameResponse {
        private Long streamId;
        private String oldTopic;
        private String newTopic;
        private Integer messagesUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MergeRequest {
        private String sourceTopic;
        private String targetTopic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MergeResponse {
        private Long streamId;
        private String sourceTopic;
        private String targetTopic;
        private Integer messagesMoved;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoResponse {
        private Long streamId;
        private String topic;
        private Long messageCount;
        private String lastMessageTime;
    }

    // Topic Subscription DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionResponse {
        private Long id;
        private Long userId;
        private Long streamId;
        private String topicName;
        private String subscriptionType;
        private Boolean desktopNotifications;
        private Boolean emailNotifications;
        private Boolean pushNotifications;
        private Boolean soundNotifications;
        private Boolean isMuted;
        private String dateSubscribed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscribeRequest {
        private Long streamId;
        private String topicName;
        private String subscriptionType;
        private Boolean desktopNotifications;
        private Boolean emailNotifications;
        private Boolean pushNotifications;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchSubscribeRequest {
        private List<SubscribeRequest> subscriptions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MuteRequest {
        private Long streamId;
        private String topicName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicSubscriptionListResponse {
        private List<SubscriptionResponse> subscriptions;
        private Long total;
    }
}
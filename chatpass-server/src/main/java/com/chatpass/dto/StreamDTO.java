package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stream DTO
 */
public class StreamDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String name;
        private String description;
        private Boolean inviteOnly = false;
        private Boolean isWebPublic = false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private Boolean inviteOnly;
        private Boolean isWebPublic;
        private Boolean deactivated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String renderedDescription;
        private Long realmId;
        private Boolean inviteOnly;
        private Boolean isWebPublic;
        private Boolean deactivated;
        private Integer subscriberCount;
        private LocalDateTime dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionResponse {
        private Long streamId;
        private String streamName;
        private Boolean subscribed;
        private String color;
        private Boolean isMuted;
        private Boolean pinToTop;
    }
}
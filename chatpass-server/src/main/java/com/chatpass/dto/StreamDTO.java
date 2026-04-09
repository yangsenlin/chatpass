package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
        private Boolean inviteOnly;
        private Boolean isWebPublic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private Boolean isMuted;
        private String color;
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
        private Boolean inviteOnly;
        private Boolean isWebPublic;
        private Boolean deactivated;
        private Integer subscriberCount;
        private Integer streamPostPolicy;
        private LocalDateTime dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionResponse {
        private Long streamId;
        private String name;
        private String color;
        private Boolean isMuted;
        private Boolean pinnedToTop;
        private Boolean inHomeView;
    }
}
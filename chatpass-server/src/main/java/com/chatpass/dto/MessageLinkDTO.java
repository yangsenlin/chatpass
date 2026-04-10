package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MessageLink DTO
 */
public class MessageLinkDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long messageId;
        private Long targetMessageId;
        private String linkType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForwardRequest {
        private Long sourceMessageId;
        private Long recipientId;
        private String topic;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long messageId;
        private Long targetMessageId;
        private String linkType;
        private MessageDTO.Response targetMessage;
        private LocalDateTime dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReferencePreview {
        private Long messageId;
        private Long targetMessageId;
        private String preview;
        private String senderName;
        private String streamName;
        private String topic;
        private LocalDateTime dateSent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private Long messageId;
        private List<Response> references;
        private List<Response> referencedBy;
    }
}
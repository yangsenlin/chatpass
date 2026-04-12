package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 消息 DTO
 */
public class MessageDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendRequest {
        private String type; // "stream" or "private"
        private Long to; // stream_id or user_id list
        private String subject; // topic name
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String subject;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String type;
        private String subject;
        private String content;
        private String renderedContent;
        private Long senderId;
        private String senderFullName;
        private String senderEmail;
        private String senderAvatarUrl;
        private Long recipientId;
        private Long streamId;
        private String streamName;
        private LocalDateTime dateSent;
        private LocalDateTime lastEditTime;
        private Boolean hasAttachment;
        private Boolean hasImage;
        private Boolean hasLink;
        private Boolean isChannelMessage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private java.util.List<Response> messages;
        private String anchor;
        private Boolean historyLimited;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RenderRequest {
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RenderResponse {
        private String content;
        private String renderedContent;
        private java.util.List<String> mentionedUsers;
        private Boolean hasWildcardMention;
    }
}
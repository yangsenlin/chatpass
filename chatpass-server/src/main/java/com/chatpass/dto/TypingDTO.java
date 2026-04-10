package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Typing DTO
 */
public class TypingDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartRequest {
        private Long recipientId;   // Stream ID or DM recipient
        private String topic;       // For Stream messages
        private String type;        // "stream" or "private"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StopRequest {
        private Long recipientId;
        private String topic;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Event {
        private String type = "typing";
        private String op;          // "start" or "stop"
        private Long senderId;
        private String senderName;
        private Long recipientId;
        private String topic;
        private List<Long> userIds; // Who should receive this event
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusResponse {
        private Long recipientId;
        private String topic;
        private List<TypingUser> typingUsers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypingUser {
        private Long id;
        private String fullName;
        private String email;
    }
}
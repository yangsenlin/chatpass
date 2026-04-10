package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TypingDTO
 * 
 * 输入提示数据传输对象
 */
public class TypingDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypingResponse {
        private Long id;
        private Long userId;
        private String userName;
        private String typingType; // direct or stream
        private Long recipientId;  // 私信接收者
        private Long streamId;     // 频道 ID
        private String topic;      // 频道 Topic
        private String lastUpdate;
        private Integer remainingSeconds;
        private Boolean isStillTyping;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StreamTypingRequest {
        private String topic; // 频道 Topic
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypingSummary {
        private Long recipientId;
        private Long typingUserCount;
        private List<String> typingUsers; // 正在输入的用户名列表
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypingEvent {
        private String eventType; // "typing_start" or "typing_stop"
        private Long userId;
        private String userName;
        private String typingType;
        private Long recipientId;
        private Long streamId;
        private String topic;
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypingListResponse {
        private List<TypingResponse> typingUsers;
        private Long count;
        private String recipientType;
    }
}
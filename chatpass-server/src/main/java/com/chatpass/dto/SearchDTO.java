package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SearchDTO
 * 
 * 搜索数据传输对象
 */
public class SearchDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchResponse {
        private String query;
        private Long streamId;
        private Long senderId;
        private String topic;
        private List<MessageResult> results;
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageResult {
        private Long messageId;
        private Long senderId;
        private String senderName;
        private Long streamId;
        private String streamName;
        private String topic;
        private String content;
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdvancedRequest {
        private String query;
        private Long streamId;
        private Long senderId;
        private String topic;
        private String dateFrom; // ISO datetime string
        private String dateTo;   // ISO datetime string
        private Integer limit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestionResponse {
        private String query;
        private List<String> suggestions;
    }
}
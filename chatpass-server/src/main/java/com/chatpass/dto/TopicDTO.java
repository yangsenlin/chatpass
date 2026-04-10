package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TopicDTO
 * 
 * 话题数据传输对象
 */
public class TopicDTO {

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
}
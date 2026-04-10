package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MessageEditDTO
 * 
 * 消息编辑数据传输对象
 */
public class MessageEditDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditHistoryResponse {
        private Long id;
        private Long messageId;
        private Long editorId;
        private String editorName;
        private String prevContent;
        private String prevTopic;
        private String newContent;
        private String newTopic;
        private String editTime;
        private Integer editType;
        private Boolean isContentEdit;
        private Boolean isTopicEdit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditSummary {
        private Long messageId;
        private Long editCount;
        private String lastEditTime;
        private String lastEditorName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditRequest {
        private String content; // 新内容
        private String topic;   // 新 Topic
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditHistoryList {
        private Long messageId;
        private java.util.List<EditHistoryResponse> history;
        private Long totalCount;
    }
}
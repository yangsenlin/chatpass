package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * UserMessage DTO
 * 
 * 用户消息状态操作
 */
public class UserMessageDTO {

    /**
     * Flags 操作请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlagsRequest {
        private List<Long> messages;   // 消息 ID 列表
        private String operation;      // "add" or "remove"
        private String flag;           // "read", "starred", "collapsed"
        private Boolean all;           // 是否操作全部（用于 narrow 查询）
    }

    /**
     * Flags 操作响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlagsResponse {
        private List<Long> messages;   // 受影响的消息 ID
        private String flag;
        private Boolean added;         // true=add, false=remove
    }

    /**
     * 未读消息摘要
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnreadSummary {
        private Long unreadCount;
        private List<UnreadStream> unreadStreams;
        private List<UnreadHuddle> unreadHuddles;
        private List<UnreadPm> unreadPms;
        private List<Long> mentionedMessages;
    }

    /**
     * Stream 未读统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnreadStream {
        private Long streamId;
        private String streamName;
        private Integer unreadCount;
        private List<UnreadTopic> topics;
    }

    /**
     * Topic 未读统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnreadTopic {
        private String topic;
        private Integer unreadCount;
        private List<Long> messageIds;
    }

    /**
     * 私信组未读统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnreadHuddle {
        private List<Long> userIds;
        private Integer unreadCount;
        private List<Long> messageIds;
    }

    /**
     * 单人私信未读统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnreadPm {
        private Long senderId;
        private String senderName;
        private Integer unreadCount;
        private List<Long> messageIds;
    }

    /**
     * Flag 类型常量
     */
    public static class FlagTypes {
        public static final String READ = "read";
        public static final String STARRED = "starred";
        public static final String COLLAPSED = "collapsed";
        public static final String MENTIONED = "mentioned";
        public static final String WILDCARD_MENTIONED = "wildcard_mentioned";
        public static final String HAS_ALERT_WORD = "has_alert_word";
        public static final String HISTORICAL = "historical";
    }
}
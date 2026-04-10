package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * Narrow 查询 DTO
 * 
 * Zulip 的消息过滤机制
 * 支持多种过滤条件组合
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NarrowDTO {

    /**
     * Narrow 查询请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private List<Filter> narrow;
        private Long anchor;           // 起始消息 ID
        private Integer numBefore;     // 向前查询数量
        private Integer numAfter;      // 向后查询数量
        private Boolean includeAnchor;
    }

    /**
     * 过滤条件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Filter {
        private String operator;       // 过滤操作符
        private String operand;        // 过滤值
    }

    /**
     * Narrow 查询响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private List<MessageDTO.Response> messages;
        private String anchor;         // 当前锚点消息 ID
        private Boolean historyLimited;
        private Boolean anchoredToNewest;
        private Boolean anchoredToOldest;
        private Long foundNewest;
        private Long foundOldest;
        private Long foundAnchor;
    }

    /**
     * Narrow 操作符常量
     */
    public static class Operators {
        public static final String STREAM = "stream";
        public static final String TOPIC = "topic";
        public static final String PRIVATE = "is-private";
        public static final String MENTIONED = "is-mentioned";
        public static final String STARRED = "is-starred";
        public static final String UNREAD = "is-unread";
        public static final String SEARCH = "search";
        public static final String SENDER = "sender";
        public static final String DATE = "date";
        public static final String HAS = "has";
        public static final String IN = "in";
    }

    /**
     * Narrow operand 值
     */
    public static class Operands {
        public static final String PRIVATE = "private";
        public static final String STARRED = "starred";
        public static final String MENTIONED = "mentioned";
        public static final String UNREAD = "unread";
        public static final String ALL = "all";
        public static final String HOME = "home";
        public static final String LINK = "link";
        public static final String IMAGE = "image";
        public static final String ATTACHMENT = "attachment";
        public static final String ALERT_WORD = "alert_word";
    }
}
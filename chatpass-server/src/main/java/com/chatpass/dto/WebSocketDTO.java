package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WebSocket Event DTO
 * 
 * Zulip 实时推送事件格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketDTO {

    /**
     * WebSocket Event 基类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Event {
        private String type;           // 事件类型
        private Long id;               // 事件 ID（递增）
        private Map<String, Object> data;
    }

    /**
     * 新消息事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageEvent {
        private String type = "message";
        private Long id;
        private MessageDTO.Response message;
        private List<Long> flags;      // 用户对该消息的 flags
        private LocalDateTime timestamp;
    }

    /**
     * 消息更新事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateMessageEvent {
        private String type = "update_message";
        private Long id;
        private Long messageId;
        private String renderedContent;
        private String subject;        // 新 Topic
        private String prevSubject;    // 原 Topic
        private Long editTimestamp;
        private Long userId;
        private List<Long> moveMessages; // 移动到新 Topic 的消息 ID
    }

    /**
     * 消息删除事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteMessageEvent {
        private String type = "delete_message";
        private Long id;
        private Long messageId;
    }

    /**
     * 已读状态更新事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReadEvent {
        private String type = "read";
        private Long id;
        private List<Long> messageIds;
    }

    /**
     * 标记事件（starred/unstarred）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlagEvent {
        private String type;           // "starred" or "unstarred"
        private Long id;
        private List<Long> messageIds;
        private String flag;           // 操作的 flag 类型
        private Boolean all;           // 是否全部标记
    }

    /**
     * Topic 操作事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicEvent {
        private String type = "topic";
        private Long id;
        private Long streamId;
        private String topic;
        private String operation;      // "create", "update", "delete"
        private String newTopic;       // 新 Topic 名称（update 时）
    }

    /**
     * Stream 操作事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StreamEvent {
        private String type = "stream";
        private Long id;
        private Long streamId;
        private String operation;      // "create", "update", "delete"
        private StreamDTO.Response stream;
    }

    /**
     * 订阅事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionEvent {
        private String type = "subscription";
        private Long id;
        private Long userId;
        private Long streamId;
        private String operation;      // "add", "remove"
    }

    /**
     * 心跳事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeartbeatEvent {
        private String type = "heartbeat";
        private Long id;
    }

    /**
     * 用户连接状态
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PresenceEvent {
        private String type = "presence";
        private Long id;
        private Long userId;
        private String status;         // "active", "idle", "offline"
        private LocalDateTime timestamp;
    }
}
package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ScheduledMessage DTO
 * 
 * 定时消息数据传输对象
 */
public class ScheduledMessageDTO {

    /**
     * 创建定时频道消息请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateStreamMessageRequest {
        /**
         * 频道ID
         */
        private Long streamId;

        /**
         * 话题名称
         */
        private String topic;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 定时发送时间（Unix 时间戳）
         */
        private Long scheduledDeliveryTimestamp;

        /**
         * 提醒目标消息ID（可选，用于提醒）
         */
        private Long reminderTargetMessageId;

        /**
         * 提醒备注（可选）
         */
        private String reminderNote;
    }

    /**
     * 创建定时私聊消息请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDirectMessageRequest {
        /**
         * 接收者用户ID列表（私聊）
         */
        private List<Long> to;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 定时发送时间（Unix 时间戳）
         */
        private Long scheduledDeliveryTimestamp;

        /**
         * 提醒目标消息ID（可选，用于提醒）
         */
        private Long reminderTargetMessageId;

        /**
         * 提醒备注（可选）
         */
        private String reminderNote;
    }

    /**
     * 更新定时时间请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTimestampRequest {
        /**
         * 新的定时发送时间（Unix 时间戳）
         */
        private Long scheduledDeliveryTimestamp;
    }

    /**
     * 定时频道消息响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamMessageResponse {
        /**
         * 定时消息ID
         */
        private Long scheduledMessageId;

        /**
         * 频道ID
         */
        private Long to;

        /**
         * 消息类型
         */
        private String type;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 渲染后的内容
         */
        private String renderedContent;

        /**
         * 话题名称
         */
        private String topic;

        /**
         * 定时发送时间（Unix 时间戳）
         */
        private Long scheduledDeliveryTimestamp;

        /**
         * 是否失败
         */
        private Boolean failed;

        /**
         * 发送类型
         */
        private String deliveryType;

        /**
         * 提醒目标消息ID
         */
        private Long reminderTargetMessageId;
    }

    /**
     * 定时私聊消息响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirectMessageResponse {
        /**
         * 定时消息ID
         */
        private Long scheduledMessageId;

        /**
         * 接收者用户ID列表
         */
        private List<Long> to;

        /**
         * 消息类型
         */
        private String type;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 渲染后的内容
         */
        private String renderedContent;

        /**
         * 定时发送时间（Unix 时间戳）
         */
        private Long scheduledDeliveryTimestamp;

        /**
         * 是否失败
         */
        private Boolean failed;

        /**
         * 发送类型
         */
        private String deliveryType;

        /**
         * 提醒目标消息ID
         */
        private Long reminderTargetMessageId;
    }

    /**
     * 定时消息列表响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        /**
         * 定时消息列表
         */
        private List<?> scheduledMessages;

        /**
         * 总数
         */
        private Integer count;
    }

    /**
     * 创建响应（返回ID）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateResponse {
        /**
         * 定时消息ID
         */
        private Long scheduledMessageId;

        /**
         * 消息类型（"stream" 或 "private"）
         */
        private String type;
    }

    /**
     * 失败消息响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedMessageResponse {
        /**
         * 定时消息ID
         */
        private Long id;

        /**
         * 失败时间
         */
        private LocalDateTime requestTimestamp;

        /**
         * 失败原因
         */
        private String failureMessage;

        /**
         * 定时发送时间
         */
        private LocalDateTime scheduledTimestamp;
    }
}
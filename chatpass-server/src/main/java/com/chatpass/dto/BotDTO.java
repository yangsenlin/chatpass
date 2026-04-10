package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * BotDTO
 * 
 * 机器人数据传输对象
 */
public class BotDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BotResponse {
        private Long id;
        private String name;
        private String botType;
        private Long botUserId;
        private String apiKey;
        private Long ownerId;
        private Long realmId;
        private String avatarUrl;
        private String endpointUrl;
        private String description;
        private Boolean isActive;
        private String dateCreated;
        private List<CommandResponse> commands;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateBotRequest {
        private String name;
        private String botType;      // OUTGOING, INCOMING, GENERIC
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateBotRequest {
        private String name;
        private String description;
        private String endpointUrl;  // Outgoing Bot 的服务端点
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiKeyResponse {
        private Long botId;
        private String apiKey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommandResponse {
        private Long id;
        private String commandName;
        private String description;
        private String handler;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCommandRequest {
        private String commandName;  // 如 /weather
        private String description;
        private String handler;      // 处理器方法名或 URL
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private String apiKey;
        private Long streamId;
        private String topic;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendPrivateMessageRequest {
        private String apiKey;
        private Long recipientId;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageResponse {
        private Long botUserId;
        private Long streamId;
        private Long recipientId;
        private String topic;
        private Boolean sent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BotListResponse {
        private List<BotResponse> bots;
        private Long total;
    }
}
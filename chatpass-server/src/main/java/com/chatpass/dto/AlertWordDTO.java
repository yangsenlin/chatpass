package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * AlertWord DTO
 * 
 * 用户自定义关键词提醒
 */
public class AlertWordDTO {

    /**
     * 创建请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String word;
        private Integer matchMode;      // 1=Contains, 2=Exact, 3=Starts with, 4=Ends with
        private Boolean notifyEmail;
        private Boolean notifyPush;
        private Boolean notifyDesktop;
    }

    /**
     * 更新请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private Boolean isActive;
        private Integer matchMode;
        private Boolean notifyEmail;
        private Boolean notifyPush;
        private Boolean notifyDesktop;
    }

    /**
     * 响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String word;
        private Boolean isActive;
        private Integer matchMode;
        private Boolean notifyEmail;
        private Boolean notifyPush;
        private Boolean notifyDesktop;
        private LocalDateTime dateCreated;
    }

    /**
     * 列表响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private List<Response> alertWords;
        private Integer count;
    }

    /**
     * 批量添加请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchRequest {
        private Set<String> words;
    }
}
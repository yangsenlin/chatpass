package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NavigationView DTO
 * 
 * 导航视图相关数据传输对象
 */
public class NavigationViewDTO {

    /**
     * 创建请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String fragment;
        private Boolean isPinned;
        private String name;
        private String viewType;
    }

    /**
     * 更新请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private Boolean isPinned;
        private String name;
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
        private String fragment;
        private Boolean isPinned;
        private String name;
        private String viewType;
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
        private List<Response> navigationViews;
        private Integer count;
    }

    /**
     * 固定视图响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PinnedResponse {
        private List<Response> pinnedViews;
        private Integer count;
    }

    /**
     * 批量固定请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchPinRequest {
        private List<String> fragments;
        private Boolean isPinned;
    }
}
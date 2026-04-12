package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RealmFilter DTO
 * 
 * 链接转换器相关数据传输对象
 */
public class RealmFilterDTO {

    /**
     * 创建请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String pattern;
        private String urlTemplate;
        private String exampleInput;
        private String reverseTemplate;
        private List<String> alternativeUrlTemplates;
    }

    /**
     * 更新请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String pattern;
        private String urlTemplate;
        private String exampleInput;
        private String reverseTemplate;
        private List<String> alternativeUrlTemplates;
        private Integer order;
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
        private String pattern;
        private String urlTemplate;
        private String exampleInput;
        private String reverseTemplate;
        private List<String> alternativeUrlTemplates;
        private Integer order;
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
        private List<Response> filters;
        private Integer count;
    }

    /**
     * 顺序更新请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderUpdateRequest {
        private Long id;
        private Integer order;
    }

    /**
     * 批量顺序更新请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchOrderUpdateRequest {
        private List<OrderUpdateRequest> updates;
    }
}
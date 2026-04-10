package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ReadReceiptDTO
 * 
 * 阅读回执数据传输对象
 */
public class ReadReceiptDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long messageId;
        private Long userId;
        private String userName;
        private String readAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchRequest {
        private List<Long> messageIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchResponse {
        private List<Response> receipts;
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusResponse {
        private Long messageId;
        private Long readCount;
        private List<ReaderInfo> readers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReaderInfo {
        private Long userId;
        private String userName;
        private String readAt;
    }
}
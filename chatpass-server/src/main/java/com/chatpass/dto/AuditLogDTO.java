package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AuditLogDTO
 * 
 * 审计日志数据传输对象
 */
public class AuditLogDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditLogResponse {
        private Long id;
        private Long userId;
        private String userName;
        private String eventType;
        private String eventDescription;
        private String resourceType;
        private Long resourceId;
        private String oldValue;      // JSON 格式
        private String newValue;      // JSON 格式
        private String result;        // SUCCESS or FAILURE
        private String errorMessage;
        private String ipAddress;
        private String userAgent;
        private Long realmId;
        private String eventTime;
        private String requestId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditSummary {
        private Long realmId;
        private String timeRangeStart;
        private String timeRangeEnd;
        private Long totalOperations;
        private Long successCount;
        private Long failureCount;
        private Double failureRate;   // 失败率百分比
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventTypeCount {
        private String eventType;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditLogListResponse {
        private List<AuditLogResponse> logs;
        private Long total;
        private Integer page;
        private Integer size;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditExportRequest {
        private String format;        // JSON, CSV, PDF
        private String timeRangeStart;
        private String timeRangeEnd;
        private String eventType;     // optional filter
        private Long userId;          // optional filter
    }
}
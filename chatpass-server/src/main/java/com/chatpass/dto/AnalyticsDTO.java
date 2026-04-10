package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AnalyticsDTO
 */
public class AnalyticsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportResponse {
        private Long id;
        private Long realmId;
        private String reportType;
        private String period;
        private String startTime;
        private String endTime;
        private String reportData;
        private String summary;
        private Long creatorId;
        private String reportTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateReportRequest {
        private String reportType;
        private String period;
        private String startTime;
        private String endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickStats {
        private Long totalMessages;
        private Long totalUsers;
        private Long totalStreams;
        private Long totalReactions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportListResponse {
        private List<ReportResponse> reports;
        private Long total;
    }
}
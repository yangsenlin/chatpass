package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据分析DTO
 */
public class AnalyticsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataPoint {
        private Long id;
        private Long realmId;
        private String dataType;
        private String period;
        private LocalDateTime timestamp;
        private Long metricValue;
        private Map<String, Object> details;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long realmId;
        private String dataType;
        private Long totalValue;
        private LocalDateTime since;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageStats {
        private Long realmId;
        private Long totalMessages;
        private String period;
        private Map<String, Object> details;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserActivityStats {
        private Long realmId;
        private Long activeUsers;
        private String period;
        private Map<String, Object> details;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StreamUsageStats {
        private Long realmId;
        private Long streamCount;
        private String period;
        private Map<String, Object> details;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Report {
        private Long realmId;
        private MessageStats messageStats;
        private UserActivityStats userActivityStats;
        private StreamUsageStats streamUsageStats;
        private LocalDateTime generatedAt;
    }
}

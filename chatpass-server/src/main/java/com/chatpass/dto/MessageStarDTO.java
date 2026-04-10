package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MessageStarDTO
 * 
 * 消息收藏数据传输对象
 */
public class MessageStarDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StarResponse {
        private Long id;
        private Long messageId;
        private Long userId;
        private String userName;
        private String starredTime;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StarRequest {
        private String note; // 收藏备注（可选）
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NoteRequest {
        private String note; // 更新备注
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StarStatus {
        private Long messageId;
        private Boolean starred;
        private Long starCount; // 该消息被收藏的总次数
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StarSummary {
        private Long userId;
        private Long totalStars;
        private List<Long> recentStarIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchRequest {
        private List<Long> messageIds; // 批量操作的消息 ID
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StarListResponse {
        private List<StarResponse> stars;
        private Long total;
        private int page;
        private int size;
    }
}
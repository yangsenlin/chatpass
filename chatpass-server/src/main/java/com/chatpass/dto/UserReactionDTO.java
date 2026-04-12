package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户反应DTO
 */
public class UserReactionDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReactionInfo {
        private Long id;
        private Long messageId;
        private Long userId;
        private String reactionType;
        private LocalDateTime reactedAt;
        private Long realmId;
        
        private String userName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReactionStats {
        private Long messageId;
        private Map<String, Long> reactionCounts; // emoji -> count
        private Map<String, List<Long>> reactionUsers; // emoji -> user ids
        private Long totalReactions;
    }
}

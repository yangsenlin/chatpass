package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息投票DTO
 */
public class MessageVoteDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoteInfo {
        private Long id;
        private Long messageId;
        private Long userId;
        private String voteType;
        private LocalDateTime votedAt;
        private Long realmId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoteStats {
        private Long messageId;
        private Long upvoteCount;
        private Long downvoteCount;
        private Long totalVotes;
        private List<Long> upvoters;
        private List<Long> downvoters;
    }
}

package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PollDTO
 */
public class PollDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PollResponse {
        private Long id;
        private Long messageId;
        private Long creatorId;
        private String question;
        private List<String> options;
        private String pollType;
        private Boolean isAnonymous;
        private Boolean allowChange;
        private String endTime;
        private String status;
        private Long totalVotes;
        private String dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePollRequest {
        private Long messageId;
        private String question;
        private List<String> options;
        private String pollType;
        private Boolean isAnonymous;
        private Boolean allowChange;
        private String endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoteRequest {
        private Long pollId;
        private Integer optionIndex;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MultiVoteRequest {
        private Long pollId;
        private List<Integer> optionIndices;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionStats {
        private Integer index;
        private String text;
        private Long votes;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PollStats {
        private Long pollId;
        private String question;
        private Long totalVotes;
        private Long totalVoters;
        private List<OptionStats> optionStats;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserVoteResponse {
        private Long pollId;
        private Long userId;
        private List<Integer> optionIndices;
    }
}
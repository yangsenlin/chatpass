package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ArchiveDTO
 */
public class ArchiveDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArchiveResponse {
        private Long id;
        private Long realmId;
        private String archiveId;
        private Long messageId;
        private String storageType;
        private String archiveReason;
        private String archiveDate;
        private String expireDate;
        private Boolean isDeleted;
        private Integer restoreCount;
        private String lastRestored;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArchiveRequest {
        private Long messageId;
        private String originalContent;
        private String storageType;
        private String archiveReason;
        private String expireDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RestoreResponse {
        private String archiveId;
        private Long messageId;
        private String originalContent;
        private Integer restoreCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArchiveListResponse {
        private List<ArchiveResponse> archives;
        private Long total;
    }
}
package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Attachment DTO
 */
public class AttachmentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadRequest {
        private String fileName;
        private String contentType;
        private Long fileSize;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long messageId;
        private String fileName;
        private String originalFileName;
        private String url;
        private String thumbnailUrl;
        private Long fileSize;
        private String contentType;
        private Integer fileType;
        private Integer width;
        private Integer height;
        private Boolean isImage;
        private LocalDateTime dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private Long messageId;
        private List<Response> attachments;
        private Integer count;
        private Long totalSize;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadResponse {
        private String url;
        private String uploadUrl;
        private String pathId;
        private Long maxSize;
    }
}
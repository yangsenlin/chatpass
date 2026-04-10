package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CustomEmojiDTO
 * 
 * 自定义表情数据传输对象
 */
public class CustomEmojiDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmojiResponse {
        private Long id;
        private String name;
        private String displayName;
        private String imageUrl;
        private Long authorId;
        private String authorName;
        private Boolean deactivated;
        private String dateCreated;
        private String emojiCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String name;         // 表情名称（必填）
        private String displayName;  // 显示名称（可选）
        private String imageUrl;     // 图片 URL（必填）
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String displayName;  // 更新显示名称
        private String imageUrl;     // 更新图片 URL
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmojiListResponse {
        private List<EmojiResponse> emojis;
        private Long count;
    }
}
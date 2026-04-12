package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息阅读状态DTO
 */
public class MessageReadStatusDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReadStatus {
        private Long id;
        private Long userId;
        private Long messageId;
        private LocalDateTime readAt;
        private Long realmId;
        
        private String userName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReadStats {
        private Long messageId;
        private Long readCount;
        private Long unreadCount;
        private List<Long> readUsers;
    }
}

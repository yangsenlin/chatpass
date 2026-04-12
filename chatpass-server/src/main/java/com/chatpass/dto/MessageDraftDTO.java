package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 消息草稿DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDraftDTO {
    
    private Long id;
    private Long userId;
    private Long streamId;
    private String toUserIds;
    private String topic;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

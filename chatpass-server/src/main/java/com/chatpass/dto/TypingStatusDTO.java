package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 输入状态DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingStatusDTO {
    
    private Long id;
    private Long userId;
    private Long streamId;
    private String topic;
    private String toUserIds;
    private Long realmId;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    
    /**
     * 用户名称（可选）
     */
    private String userName;
}

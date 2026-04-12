package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户状态DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusDTO {
    
    private Long id;
    private Long userId;
    private String statusText;
    private String statusEmoji;
    private Integer durationSeconds;
    private LocalDateTime expiresAt;
    private Long realmId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String userName;
}

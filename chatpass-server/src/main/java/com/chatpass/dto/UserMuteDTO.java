package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户静音DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMuteDTO {
    
    private Long id;
    private Long userId;
    private Long mutedUserId;
    private Long realmId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 被静音用户名称（可选）
     */
    private String mutedUserName;
}

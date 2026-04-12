package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户在线状态DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresenceDTO {
    
    private Long id;
    private Long userId;
    private String status;
    private String statusMessage;
    private LocalDateTime lastActive;
    private LocalDateTime lastOffline;
    private Long realmId;
    private LocalDateTime updatedAt;
    private Boolean pushNotifications;
    private Boolean showOffline;
    
    /**
     * 用户名称（可选）
     */
    private String userName;
}

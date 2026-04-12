package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 设备DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO {
    
    private Long id;
    private Long userId;
    private String deviceType;
    private String deviceName;
    private String os;
    private String browser;
    private String ipAddress;
    private String deviceId;
    private LocalDateTime lastActive;
    private LocalDateTime lastLogin;
    private Boolean isCurrent;
    private Boolean pushNotificationsEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

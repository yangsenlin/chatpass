package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 组织配置DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealmSettingsDTO {
    
    private Long id;
    private Long realmId;
    private String settingKey;
    private String settingValue;
    private String settingType;
    private String description;
    private Boolean editable;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

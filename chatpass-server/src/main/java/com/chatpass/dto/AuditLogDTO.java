package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 审计日志DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    
    private Long id;
    private Long actorId;
    private String eventType;
    private LocalDateTime eventTime;
    private Long realmId;
    private String objectType;
    private Long objectId;
    private String extraData;
    private String ipAddress;
    private String userAgent;
    private String result;
    private String errorMessage;
    
    /**
     * 操作者名称（可选）
     */
    private String actorName;
}

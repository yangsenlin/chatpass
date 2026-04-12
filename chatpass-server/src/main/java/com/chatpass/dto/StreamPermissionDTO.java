package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Stream权限DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamPermissionDTO {
    
    private Long id;
    private Long streamId;
    private Long userId;
    private String permissionType;
    private Boolean canRead;
    private Boolean canWrite;
    private Boolean canModifyTopic;
    private Boolean canManageMembers;
    private Boolean canDeleteMessages;
    private Long realmId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 用户名称（可选）
     */
    private String userName;
    
    /**
     * Stream名称（可选）
     */
    private String streamName;
}

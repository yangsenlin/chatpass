package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 固定消息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinnedMessageDTO {
    
    private Long id;
    private Long messageId;
    private Long streamId;
    private String topic;
    private Long realmId;
    private Long pinnedBy;
    private LocalDateTime pinnedAt;
    private Integer sortOrder;
    private Boolean isExpired;
    private LocalDateTime expiresAt;
    
    /**
     * 消息内容（可选）
     */
    private String messageContent;
    
    /**
     * 消息发送者名称（可选）
     */
    private String senderName;
}

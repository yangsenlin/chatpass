package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 消息归档DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageArchiveDTO {
    
    private Long id;
    private Long originalMessageId;
    private String content;
    private String renderedContent;
    private Long senderId;
    private Long streamId;
    private String topic;
    private Long realmId;
    private LocalDateTime originalDateSent;
    private LocalDateTime archivedAt;
    private String archivePolicy;
    private Long archivedBy;
    private Boolean isRecoverable;
    private LocalDateTime recoverUntil;
    
    /**
     * 发送者名称（可选）
     */
    private String senderName;
}

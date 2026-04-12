package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息标记DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageFlagDTO {
    
    private Long id;
    private Long userId;
    private Long messageId;
    private LocalDateTime flaggedAt;
    private Long realmId;
    private String flagType;
    
    private String userName;
}

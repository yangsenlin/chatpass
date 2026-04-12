package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 话题订阅DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicSubscriptionDTO {
    
    private Long id;
    private Long userId;
    private Long streamId;
    private String topic;
    private Long realmId;
    private Boolean isMuted;
    private String notificationSettings;
    private LocalDateTime subscribedAt;
    private LocalDateTime updatedAt;
}

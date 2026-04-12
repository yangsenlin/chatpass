package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 频道分类DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelFolderDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long realmId;
    private Integer sortOrder;
    private Boolean isDefault;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 分类下的频道数量
     */
    private Integer channelCount;
}

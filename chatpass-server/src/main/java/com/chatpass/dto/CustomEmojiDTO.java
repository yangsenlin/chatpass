package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自定义表情DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomEmojiDTO {
    
    private Long id;
    private String name;
    private String aliases;
    private String imageUrl;
    private Long realmId;
    private Long authorId;
    private Boolean deactivated;
    private Integer usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 别名列表
     */
    private List<String> aliasList;
}

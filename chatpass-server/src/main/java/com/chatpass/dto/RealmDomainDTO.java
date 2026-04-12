package com.chatpass.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 组织域名DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealmDomainDTO {
    
    private Long id;
    private String domain;
    private Long realmId;
    private Boolean isPrimary;
    private Boolean allowSubdomains;
    private String status;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

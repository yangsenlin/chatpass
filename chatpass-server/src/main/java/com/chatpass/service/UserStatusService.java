package com.chatpass.service;

import com.chatpass.dto.UserStatusDTO;
import com.chatpass.entity.UserStatus;
import com.chatpass.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户状态服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusService {
    
    private final UserStatusRepository statusRepository;
    
    /**
     * 设置用户状态
     */
    @Transactional
    public UserStatusDTO setUserStatus(Long userId, String statusText, String statusEmoji,
                                        Integer durationSeconds, Long realmId) {
        
        UserStatus status = statusRepository.findByUserId(userId)
                .orElseGet(() -> UserStatus.builder().userId(userId).realmId(realmId).build());
        
        status.setStatusText(statusText);
        status.setStatusEmoji(statusEmoji);
        status.setDurationSeconds(durationSeconds);
        
        // 设置过期时间
        if (durationSeconds != null && durationSeconds > 0) {
            status.setExpiresAt(LocalDateTime.now().plusSeconds(durationSeconds));
        } else {
            status.setExpiresAt(null);
        }
        
        status = statusRepository.save(status);
        log.info("设置用户状态: userId={}, text={}", userId, statusText);
        
        return toDTO(status);
    }
    
    /**
     * 清除用户状态
     */
    @Transactional
    public void clearUserStatus(Long userId) {
        statusRepository.deleteByUserId(userId);
        log.info("清除用户状态: userId={}", userId);
    }
    
    /**
     * 获取用户状态
     */
    public Optional<UserStatusDTO> getUserStatus(Long userId) {
        return statusRepository.findByUserId(userId)
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::toDTO);
    }
    
    /**
     * 获取组织的用户状态
     */
    public List<UserStatusDTO> getRealmUserStatuses(Long realmId) {
        return statusRepository.findActiveStatusesByRealm(realmId, LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有有效状态
     */
    public List<UserStatusDTO> getActiveStatuses() {
        return statusRepository.findActiveStatuses(LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 清理过期状态
     */
    @Transactional
    public int cleanupExpiredStatuses() {
        statusRepository.clearExpiredStatuses(LocalDateTime.now());
        log.info("清理过期状态");
        return 0;
    }
    
    private UserStatusDTO toDTO(UserStatus status) {
        return UserStatusDTO.builder()
                .id(status.getId())
                .userId(status.getUserId())
                .statusText(status.getStatusText())
                .statusEmoji(status.getStatusEmoji())
                .durationSeconds(status.getDurationSeconds())
                .expiresAt(status.getExpiresAt())
                .realmId(status.getRealmId())
                .createdAt(status.getCreatedAt())
                .updatedAt(status.getUpdatedAt())
                .build();
    }
}

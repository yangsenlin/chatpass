package com.chatpass.service;

import com.chatpass.dto.UserPresenceDTO;
import com.chatpass.entity.UserPresence;
import com.chatpass.repository.UserPresenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户在线状态服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceService {
    
    private final UserPresenceRepository presenceRepository;
    
    /**
     * 设置用户在线状态
     */
    @Transactional
    public UserPresenceDTO setPresence(Long userId, String status, String statusMessage, Long realmId) {
        
        UserPresence presence = presenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPresence.builder()
                        .userId(userId)
                        .realmId(realmId)
                        .status("offline")
                        .build());
        
        presence.setStatus(status);
        presence.setStatusMessage(statusMessage);
        
        if ("online".equals(status)) {
            presence.setLastActive(LocalDateTime.now());
        } else if ("offline".equals(status)) {
            presence.setLastOffline(LocalDateTime.now());
        }
        
        presence = presenceRepository.save(presence);
        log.info("更新用户状态: userId={}, status={}", userId, status);
        
        return toDTO(presence);
    }
    
    /**
     * 用户上线
     */
    @Transactional
    public UserPresenceDTO markOnline(Long userId, Long realmId) {
        return setPresence(userId, "online", null, realmId);
    }
    
    /**
     * 用户离线
     */
    @Transactional
    public UserPresenceDTO markOffline(Long userId, Long realmId) {
        return setPresence(userId, "offline", null, realmId);
    }
    
    /**
     * 设置空闲状态
     */
    @Transactional
    public UserPresenceDTO markIdle(Long userId, Long realmId) {
        return setPresence(userId, "idle", null, realmId);
    }
    
    /**
     * 设置忙碌状态
     */
    @Transactional
    public UserPresenceDTO markBusy(Long userId, String statusMessage, Long realmId) {
        return setPresence(userId, "busy", statusMessage, realmId);
    }
    
    /**
     * 获取用户状态
     */
    public Optional<UserPresenceDTO> getUserPresence(Long userId) {
        return presenceRepository.findByUserId(userId)
                .map(this::toDTO);
    }
    
    /**
     * 获取组织的所有用户状态
     */
    public List<UserPresenceDTO> getRealmPresences(Long realmId) {
        return presenceRepository.findByRealmId(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取在线用户
     */
    public List<UserPresenceDTO> getOnlineUsers(Long realmId) {
        return presenceRepository.findOnlineUsers(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取活跃用户
     */
    public List<UserPresenceDTO> getActiveUsers(Long realmId, LocalDateTime since) {
        return presenceRepository.findActiveUsers(realmId, since)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 统计在线用户数量
     */
    public long countOnlineUsers(Long realmId) {
        return presenceRepository.countOnlineUsers(realmId);
    }
    
    /**
     * 设置状态消息
     */
    @Transactional
    public UserPresenceDTO setStatusMessage(Long userId, String statusMessage) {
        UserPresence presence = presenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户状态不存在"));
        
        presence.setStatusMessage(statusMessage);
        presence = presenceRepository.save(presence);
        
        return toDTO(presence);
    }
    
    /**
     * 设置推送通知
     */
    @Transactional
    public void setPushNotifications(Long userId, boolean enabled) {
        UserPresence presence = presenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户状态不存在"));
        
        presence.setPushNotifications(enabled);
        presenceRepository.save(presence);
    }
    
    /**
     * 设置显示离线状态
     */
    @Transactional
    public void setShowOffline(Long userId, boolean showOffline) {
        UserPresence presence = presenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户状态不存在"));
        
        presence.setShowOffline(showOffline);
        presenceRepository.save(presence);
    }
    
    /**
     * 清理长时间未活跃的用户（标记为离线）
     */
    @Transactional
    public int cleanupInactiveUsers(int minutesThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);
        presenceRepository.markInactiveOffline(threshold, LocalDateTime.now());
        
        log.info("清理不活跃用户: threshold={}分钟", minutesThreshold);
        return 0; // 实际应返回清理数量
    }
    
    /**
     * 更新活跃时间
     */
    @Transactional
    public void updateActivity(Long userId) {
        presenceRepository.findByUserId(userId)
                .ifPresent(p -> {
                    p.setLastActive(LocalDateTime.now());
                    presenceRepository.save(p);
                });
    }
    
    private UserPresenceDTO toDTO(UserPresence presence) {
        return UserPresenceDTO.builder()
                .id(presence.getId())
                .userId(presence.getUserId())
                .status(presence.getStatus())
                .statusMessage(presence.getStatusMessage())
                .lastActive(presence.getLastActive())
                .lastOffline(presence.getLastOffline())
                .realmId(presence.getRealmId())
                .updatedAt(presence.getUpdatedAt())
                .pushNotifications(presence.getPushNotifications())
                .showOffline(presence.getShowOffline())
                .build();
    }
}

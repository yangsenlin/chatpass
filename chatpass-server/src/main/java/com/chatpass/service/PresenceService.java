package com.chatpass.service;

import com.chatpass.entity.UserPresence;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.UserPresenceRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户在线状态服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private final UserPresenceRepository presenceRepository;
    private final UserProfileRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 更新用户状态
     */
    @Transactional
    public void updateStatus(Long userId, String status) {
        UserPresence presence = presenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    return UserPresence.builder().user(user).build();
                });

        presence.setStatus(status);
        presence.setLastActiveTime(LocalDateTime.now());
        presenceRepository.save(presence);

        // 广播状态变化
        broadcastPresence(userId, status);

        log.debug("User {} status updated to {}", userId, status);
    }

    /**
     * 获取用户状态
     */
    @Transactional(readOnly = true)
    public String getStatus(Long userId) {
        return presenceRepository.findByUserId(userId)
                .map(UserPresence::getStatus)
                .orElse(UserPresence.STATUS_OFFLINE);
    }

    /**
     * 获取在线用户
     */
    @Transactional(readOnly = true)
    public List<Long> getOnlineUsers(Long realmId) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        return presenceRepository.findActiveUsers(UserPresence.STATUS_ACTIVE, threshold)
                .stream()
                .filter(p -> p.getUser().getRealm().getId().equals(realmId))
                .map(p -> p.getUser().getId())
                .toList();
    }

    /**
     * 设置用户活跃
     */
    @Transactional
    public void setActive(Long userId) {
        updateStatus(userId, UserPresence.STATUS_ACTIVE);
    }

    /**
     * 设置用户空闲
     */
    @Transactional
    public void setIdle(Long userId) {
        updateStatus(userId, UserPresence.STATUS_IDLE);
    }

    /**
     * 设置用户离线
     */
    @Transactional
    public void setOffline(Long userId) {
        updateStatus(userId, UserPresence.STATUS_OFFLINE);
    }

    private void broadcastPresence(Long userId, String status) {
        messagingTemplate.convertAndSend("/topic/presence/" + userId, 
                java.util.Map.of("userId", userId, "status", status, "timestamp", System.currentTimeMillis()));
    }
}
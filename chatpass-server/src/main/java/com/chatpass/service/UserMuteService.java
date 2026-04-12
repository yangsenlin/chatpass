package com.chatpass.service;

import com.chatpass.dto.UserMuteDTO;
import com.chatpass.entity.UserMute;
import com.chatpass.repository.UserMuteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户静音服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserMuteService {
    
    private final UserMuteRepository muteRepository;
    
    /**
     * 静音用户
     */
    @Transactional
    public UserMuteDTO muteUser(Long userId, Long mutedUserId, Long realmId) {
        
        if (userId.equals(mutedUserId)) {
            throw new IllegalArgumentException("不能静音自己");
        }
        
        if (muteRepository.existsByUserIdAndMutedUserId(userId, mutedUserId)) {
            throw new IllegalStateException("已静音该用户");
        }
        
        UserMute mute = UserMute.builder()
                .userId(userId)
                .mutedUserId(mutedUserId)
                .realmId(realmId)
                .build();
        
        mute = muteRepository.save(mute);
        log.info("静音用户: userId={}, mutedUserId={}", userId, mutedUserId);
        
        return toDTO(mute);
    }
    
    /**
     * 取消静音
     */
    @Transactional
    public void unmuteUser(Long userId, Long mutedUserId) {
        muteRepository.deleteByUserIdAndMutedUserId(userId, mutedUserId);
        log.info("取消静音: userId={}, mutedUserId={}", userId, mutedUserId);
    }
    
    /**
     * 获取用户静音列表
     */
    public List<UserMuteDTO> getUserMutes(Long userId) {
        return muteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否静音
     */
    public boolean isMuted(Long userId, Long mutedUserId) {
        return muteRepository.existsByUserIdAndMutedUserId(userId, mutedUserId);
    }
    
    /**
     * 获取静音了某用户的所有用户
     */
    public List<Long> getUsersMutingUser(Long mutedUserId) {
        return muteRepository.findUsersMutingUser(mutedUserId);
    }
    
    /**
     * 统计静音数量
     */
    public long countMutes(Long userId) {
        return muteRepository.countByUserId(userId);
    }
    
    private UserMuteDTO toDTO(UserMute mute) {
        return UserMuteDTO.builder()
                .id(mute.getId())
                .userId(mute.getUserId())
                .mutedUserId(mute.getMutedUserId())
                .realmId(mute.getRealmId())
                .createdAt(mute.getCreatedAt())
                .updatedAt(mute.getUpdatedAt())
                .build();
    }
}

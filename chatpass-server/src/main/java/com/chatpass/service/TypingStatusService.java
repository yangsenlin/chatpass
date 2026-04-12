package com.chatpass.service;

import com.chatpass.dto.TypingStatusDTO;
import com.chatpass.entity.TypingStatus;
import com.chatpass.repository.TypingStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 输入状态服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TypingStatusService {
    
    private final TypingStatusRepository typingRepository;
    
    /**
     * 开始输入状态
     */
    @Transactional
    public TypingStatusDTO startTyping(Long userId, Long streamId, String topic, 
                                        String toUserIds, Long realmId) {
        
        TypingStatus typing = TypingStatus.builder()
                .userId(userId)
                .streamId(streamId)
                .topic(topic)
                .toUserIds(toUserIds)
                .realmId(realmId)
                .build();
        
        typing = typingRepository.save(typing);
        log.info("开始输入状态: userId={}, streamId={}, topic={}", userId, streamId, topic);
        
        return toDTO(typing);
    }
    
    /**
     * 停止输入状态
     */
    @Transactional
    public void stopTyping(Long userId) {
        typingRepository.clearUserTyping(userId);
        log.info("停止输入状态: userId={}", userId);
    }
    
    /**
     * 获取用户的输入状态
     */
    public List<TypingStatusDTO> getUserTypingStatus(Long userId) {
        return typingRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取话题正在输入的用户
     */
    public List<TypingStatusDTO> getTypingUsers(Long streamId, String topic) {
        return typingRepository.findByStreamIdAndTopic(streamId, topic)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取频道正在输入的用户
     */
    public List<TypingStatusDTO> getStreamTypingUsers(Long streamId) {
        return typingRepository.findByStreamIdOrderByStartedAtDesc(streamId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 统计正在输入的用户数
     */
    public long countTypingUsers(Long streamId, String topic) {
        return typingRepository.countTypingUsers(streamId, topic);
    }
    
    /**
     * 清理过期的输入状态
     */
    @Transactional
    public int cleanupExpiredTyping(int secondsThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(secondsThreshold);
        typingRepository.clearExpiredTyping(threshold);
        log.info("清理过期输入状态: threshold={}秒", secondsThreshold);
        return 0;
    }
    
    private TypingStatusDTO toDTO(TypingStatus typing) {
        return TypingStatusDTO.builder()
                .id(typing.getId())
                .userId(typing.getUserId())
                .streamId(typing.getStreamId())
                .topic(typing.getTopic())
                .toUserIds(typing.getToUserIds())
                .realmId(typing.getRealmId())
                .startedAt(typing.getStartedAt())
                .updatedAt(typing.getUpdatedAt())
                .build();
    }
}

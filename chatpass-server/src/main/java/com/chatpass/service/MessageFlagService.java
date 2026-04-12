package com.chatpass.service;

import com.chatpass.dto.MessageFlagDTO;
import com.chatpass.entity.MessageFlag;
import com.chatpass.repository.MessageFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息标记服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageFlagService {
    
    private final MessageFlagRepository flagRepository;
    
    /**
     * 标记消息
     */
    @Transactional
    public MessageFlagDTO flagMessage(Long userId, Long messageId, String flagType, Long realmId) {
        
        if (flagRepository.existsByUserIdAndMessageId(userId, messageId)) {
            throw new IllegalStateException("消息已标记");
        }
        
        MessageFlag flag = MessageFlag.builder()
                .userId(userId)
                .messageId(messageId)
                .flagType(flagType != null ? flagType : "star")
                .realmId(realmId)
                .build();
        
        flag = flagRepository.save(flag);
        log.info("标记消息: userId={}, messageId={}, type={}", userId, messageId, flagType);
        
        return toDTO(flag);
    }
    
    /**
     * 取消标记
     */
    @Transactional
    public void unflagMessage(Long userId, Long messageId) {
        flagRepository.deleteByUserIdAndMessageId(userId, messageId);
        log.info("取消标记: userId={}, messageId={}", userId, messageId);
    }
    
    /**
     * 获取用户标记列表
     */
    public List<MessageFlagDTO> getUserFlags(Long userId) {
        return flagRepository.findByUserIdOrderByFlaggedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户特定类型标记
     */
    public List<MessageFlagDTO> getUserFlagsByType(Long userId, String flagType) {
        return flagRepository.findByUserIdAndFlagTypeOrderByFlaggedAtDesc(userId, flagType)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取消息标记用户
     */
    public List<MessageFlagDTO> getMessageFlags(Long messageId) {
        return flagRepository.findByMessageIdOrderByFlaggedAtAsc(messageId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否已标记
     */
    public boolean isFlagged(Long userId, Long messageId) {
        return flagRepository.existsByUserIdAndMessageId(userId, messageId);
    }
    
    /**
     * 获取标记用户列表
     */
    public List<Long> getFlagUsers(Long messageId) {
        return flagRepository.findFlagUsers(messageId);
    }
    
    /**
     * 统计用户标记数
     */
    public long countUserFlags(Long userId) {
        return flagRepository.countByUserId(userId);
    }
    
    /**
     * 统计消息标记数
     */
    public long countMessageFlags(Long messageId) {
        return flagRepository.countByMessageId(messageId);
    }
    
    private MessageFlagDTO toDTO(MessageFlag flag) {
        return MessageFlagDTO.builder()
                .id(flag.getId())
                .userId(flag.getUserId())
                .messageId(flag.getMessageId())
                .flaggedAt(flag.getFlaggedAt())
                .realmId(flag.getRealmId())
                .flagType(flag.getFlagType())
                .build();
    }
}

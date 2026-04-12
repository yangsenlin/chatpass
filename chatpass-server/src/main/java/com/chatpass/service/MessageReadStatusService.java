package com.chatpass.service;

import com.chatpass.dto.MessageReadStatusDTO;
import com.chatpass.entity.MessageReadStatus;
import com.chatpass.repository.MessageReadStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息阅读状态服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageReadStatusService {
    
    private final MessageReadStatusRepository readStatusRepository;
    
    /**
     * 标记消息已读
     */
    @Transactional
    public MessageReadStatusDTO.ReadStatus markAsRead(Long userId, Long messageId, Long realmId) {
        
        if (readStatusRepository.existsByUserIdAndMessageId(userId, messageId)) {
            throw new IllegalStateException("消息已标记为已读");
        }
        
        MessageReadStatus readStatus = MessageReadStatus.builder()
                .userId(userId)
                .messageId(messageId)
                .realmId(realmId)
                .build();
        
        readStatus = readStatusRepository.save(readStatus);
        log.info("标记消息已读: userId={}, messageId={}", userId, messageId);
        
        return toReadStatus(readStatus);
    }
    
    /**
     * 批量标记已读
     */
    @Transactional
    public void batchMarkAsRead(Long userId, List<Long> messageIds, Long realmId) {
        LocalDateTime now = LocalDateTime.now();
        
        for (Long messageId : messageIds) {
            if (!readStatusRepository.existsByUserIdAndMessageId(userId, messageId)) {
                MessageReadStatus readStatus = MessageReadStatus.builder()
                        .userId(userId)
                        .messageId(messageId)
                        .realmId(realmId)
                        .readAt(now)
                        .build();
                
                readStatusRepository.save(readStatus);
            }
        }
        
        log.info("批量标记已读: userId={}, count={}", userId, messageIds.size());
    }
    
    /**
     * 取消已读标记
     */
    @Transactional
    public void markAsUnread(Long userId, Long messageId) {
        readStatusRepository.deleteByUserIdAndMessageId(userId, messageId);
        log.info("取消已读标记: userId={}, messageId={}", userId, messageId);
    }
    
    /**
     * 获取用户已读消息列表
     */
    public List<MessageReadStatusDTO.ReadStatus> getUserReadMessages(Long userId) {
        return readStatusRepository.findByUserIdOrderByReadAtDesc(userId)
                .stream()
                .map(this::toReadStatus)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取消息已读用户列表
     */
    public List<MessageReadStatusDTO.ReadStatus> getMessageReaders(Long messageId) {
        return readStatusRepository.findByMessageIdOrderByReadAtAsc(messageId)
                .stream()
                .map(this::toReadStatus)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查消息是否已读
     */
    public boolean isRead(Long userId, Long messageId) {
        return readStatusRepository.existsByUserIdAndMessageId(userId, messageId);
    }
    
    /**
     * 获取已读用户列表
     */
    public List<Long> getReadUsers(Long messageId) {
        return readStatusRepository.findReadUsers(messageId);
    }
    
    /**
     * 统计已读用户数
     */
    public long countReaders(Long messageId) {
        return readStatusRepository.countByMessageId(messageId);
    }
    
    /**
     * 统计用户已读消息数
     */
    public long countReadMessages(Long userId) {
        return readStatusRepository.countByUserId(userId);
    }
    
    private MessageReadStatusDTO.ReadStatus toReadStatus(MessageReadStatus readStatus) {
        return MessageReadStatusDTO.ReadStatus.builder()
                .id(readStatus.getId())
                .userId(readStatus.getUserId())
                .messageId(readStatus.getMessageId())
                .readAt(readStatus.getReadAt())
                .realmId(readStatus.getRealmId())
                .build();
    }
}

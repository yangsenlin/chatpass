package com.chatpass.service;

import com.chatpass.dto.PinnedMessageDTO;
import com.chatpass.entity.Message;
import com.chatpass.entity.PinnedMessage;
import com.chatpass.repository.MessageRepository;
import com.chatpass.repository.PinnedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 固定消息服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PinnedMessageService {
    
    private final PinnedMessageRepository pinnedMessageRepository;
    private final MessageRepository messageRepository;
    
    /**
     * 固定消息
     */
    @Transactional
    public PinnedMessageDTO pinMessage(Long messageId, Long streamId, String topic,
                                         Long realmId, Long pinnedBy, LocalDateTime expiresAt) {
        
        // 检查消息是否存在
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在: " + messageId));
        
        // 检查是否已固定
        if (pinnedMessageRepository.existsByMessageId(messageId)) {
            throw new IllegalStateException("消息已固定");
        }
        
        // 获取最大排序号
        List<PinnedMessage> existing = streamId != null ?
                (topic != null ? pinnedMessageRepository.findByStreamIdAndTopicAndIsExpiredFalseOrderBySortOrderAsc(streamId, topic)
                              : pinnedMessageRepository.findByStreamIdAndIsExpiredFalseOrderBySortOrderAsc(streamId))
                : pinnedMessageRepository.findByRealmIdAndIsExpiredFalseOrderBySortOrderAsc(realmId);
        
        int sortOrder = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;
        
        PinnedMessage pinned = PinnedMessage.builder()
                .messageId(messageId)
                .streamId(streamId)
                .topic(topic)
                .realmId(realmId)
                .pinnedBy(pinnedBy)
                .sortOrder(sortOrder)
                .expiresAt(expiresAt)
                .isExpired(false)
                .build();
        
        pinned = pinnedMessageRepository.save(pinned);
        log.info("固定消息: messageId={}, streamId={}, topic={}", messageId, streamId, topic);
        
        return toDTO(pinned, message.getContent(), message.getSender().getFullName());
    }
    
    /**
     * 取消固定
     */
    @Transactional
    public void unpinMessage(Long messageId) {
        PinnedMessage pinned = pinnedMessageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息未固定"));
        
        pinnedMessageRepository.delete(pinned);
        log.info("取消固定消息: messageId={}", messageId);
    }
    
    /**
     * 获取组织的固定消息
     */
    public List<PinnedMessageDTO> getRealmPinnedMessages(Long realmId) {
        return pinnedMessageRepository.findByRealmIdAndIsExpiredFalseOrderBySortOrderAsc(realmId)
                .stream()
                .map(pm -> toDTOWithMessage(pm))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取Stream的固定消息
     */
    public List<PinnedMessageDTO> getStreamPinnedMessages(Long streamId) {
        return pinnedMessageRepository.findByStreamIdAndIsExpiredFalseOrderBySortOrderAsc(streamId)
                .stream()
                .map(pm -> toDTOWithMessage(pm))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取Topic的固定消息
     */
    public List<PinnedMessageDTO> getTopicPinnedMessages(Long streamId, String topic) {
        return pinnedMessageRepository.findByStreamIdAndTopicAndIsExpiredFalseOrderBySortOrderAsc(streamId, topic)
                .stream()
                .map(pm -> toDTOWithMessage(pm))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取固定消息详情
     */
    public Optional<PinnedMessageDTO> getPinnedMessageById(Long pinnedId) {
        return pinnedMessageRepository.findById(pinnedId)
                .map(pm -> toDTOWithMessage(pm));
    }
    
    /**
     * 更新排序
     */
    @Transactional
    public void updateSortOrder(Long pinnedId, Integer sortOrder) {
        pinnedMessageRepository.updateSortOrder(pinnedId, sortOrder);
        log.info("更新固定消息排序: pinnedId={}, sortOrder={}", pinnedId, sortOrder);
    }
    
    /**
     * 设置过期时间
     */
    @Transactional
    public void setExpiry(Long pinnedId, LocalDateTime expiresAt) {
        PinnedMessage pinned = pinnedMessageRepository.findById(pinnedId)
                .orElseThrow(() -> new IllegalArgumentException("固定消息不存在"));
        
        pinned.setExpiresAt(expiresAt);
        pinnedMessageRepository.save(pinned);
        log.info("设置固定消息过期时间: pinnedId={}, expiresAt={}", pinnedId, expiresAt);
    }
    
    /**
     * 清理过期固定消息
     */
    @Transactional
    public void cleanupExpiredMessages() {
        pinnedMessageRepository.markExpiredMessages(LocalDateTime.now());
        log.info("清理过期固定消息");
    }
    
    /**
     * 检查消息是否已固定
     */
    public boolean isMessagePinned(Long messageId) {
        return pinnedMessageRepository.existsByMessageId(messageId);
    }
    
    /**
     * 获取用户的固定消息
     */
    public List<PinnedMessageDTO> getUserPinnedMessages(Long pinnedBy) {
        return pinnedMessageRepository.findByPinnedByOrderByPinnedAtDesc(pinnedBy)
                .stream()
                .map(pm -> toDTOWithMessage(pm))
                .collect(Collectors.toList());
    }
    
    private PinnedMessageDTO toDTO(PinnedMessage pm, String messageContent, String senderName) {
        return PinnedMessageDTO.builder()
                .id(pm.getId())
                .messageId(pm.getMessageId())
                .streamId(pm.getStreamId())
                .topic(pm.getTopic())
                .realmId(pm.getRealmId())
                .pinnedBy(pm.getPinnedBy())
                .pinnedAt(pm.getPinnedAt())
                .sortOrder(pm.getSortOrder())
                .isExpired(pm.getIsExpired())
                .expiresAt(pm.getExpiresAt())
                .messageContent(messageContent)
                .senderName(senderName)
                .build();
    }
    
    private PinnedMessageDTO toDTOWithMessage(PinnedMessage pm) {
        Optional<Message> message = messageRepository.findById(pm.getMessageId());
        
        String content = message.map(Message::getContent).orElse(null);
        String senderName = message.map(m -> m.getSender().getFullName()).orElse(null);
        
        return toDTO(pm, content, senderName);
    }
}

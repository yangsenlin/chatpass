package com.chatpass.service;

import com.chatpass.dto.MessageArchiveDTO;
import com.chatpass.entity.Message;
import com.chatpass.entity.MessageArchive;
import com.chatpass.repository.MessageArchiveRepository;
import com.chatpass.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息归档服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageArchiveService {
    
    private final MessageArchiveRepository archiveRepository;
    private final MessageRepository messageRepository;
    
    /**
     * 归档消息
     */
    @Transactional
    public MessageArchiveDTO archiveMessage(Long messageId, String archivePolicy, 
                                             Long archivedBy, LocalDateTime recoverUntil) {
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在: " + messageId));
        
        // 检查是否已归档
        if (archiveRepository.findByOriginalMessageId(messageId).isPresent()) {
            throw new IllegalStateException("消息已归档");
        }
        
        MessageArchive archive = MessageArchive.builder()
                .originalMessageId(messageId)
                .content(message.getContent())
                .renderedContent(message.getRenderedContent())
                .senderId(message.getSender().getId())
                .streamId(message.getRecipient().getStreamId())
                .topic(message.getSubject())
                .realmId(message.getRealm().getId())
                .originalDateSent(message.getDateSent())
                .archivePolicy(archivePolicy)
                .archivedBy(archivedBy)
                .isRecoverable(true)
                .recoverUntil(recoverUntil)
                .build();
        
        archive = archiveRepository.save(archive);
        log.info("归档消息: messageId={}, policy={}", messageId, archivePolicy);
        
        return toDTO(archive, message.getSender().getFullName());
    }
    
    /**
     * 批量归档消息
     */
    @Transactional
    public int archiveMessagesByPolicy(Long realmId, String archivePolicy, int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        
        // 查找符合条件的消息（简化实现）
        List<Message> messages = messageRepository.findByRealmIdOrderByDateSentDesc(realmId, null)
                .getContent()
                .stream()
                .filter(m -> m.getDateSent().isBefore(threshold))
                .collect(Collectors.toList());
        
        int count = 0;
        for (Message message : messages) {
            try {
                archiveMessage(message.getId(), archivePolicy, null, LocalDateTime.now().plusDays(30));
                count++;
            } catch (Exception e) {
                log.warn("归档失败: messageId={}, error={}", message.getId(), e.getMessage());
            }
        }
        
        log.info("批量归档: realmId={}, count={}", realmId, count);
        return count;
    }
    
    /**
     * 恢复归档消息
     */
    @Transactional
    public void recoverMessage(Long archiveId) {
        MessageArchive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("归档不存在"));
        
        if (!archive.getIsRecoverable()) {
            throw new IllegalStateException("消息不可恢复");
        }
        
        if (archive.getRecoverUntil() != null && archive.getRecoverUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("恢复期限已过");
        }
        
        // 创建恢复的消息（简化实现）
        // 实际应该恢复到原位置，这里仅标记归档为已恢复
        archive.setIsRecoverable(false);
        archiveRepository.save(archive);
        
        log.info("恢复归档消息: archiveId={}", archiveId);
    }
    
    /**
     * 获取组织的归档消息
     */
    public List<MessageArchiveDTO> getRealmArchives(Long realmId) {
        return archiveRepository.findByRealmIdOrderByArchivedAtDesc(realmId)
                .stream()
                .map(this::toDTOBasic)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取Stream的归档消息
     */
    public List<MessageArchiveDTO> getStreamArchives(Long streamId) {
        return archiveRepository.findByStreamIdOrderByArchivedAtDesc(streamId)
                .stream()
                .map(this::toDTOBasic)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取可恢复的归档
     */
    public List<MessageArchiveDTO> getRecoverableArchives(Long realmId) {
        return archiveRepository.findRecoverableArchives(realmId, LocalDateTime.now())
                .stream()
                .map(this::toDTOBasic)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取归档详情
     */
    public Optional<MessageArchiveDTO> getArchiveById(Long archiveId) {
        return archiveRepository.findById(archiveId)
                .map(this::toDTOBasic);
    }
    
    /**
     * 查找原消息的归档
     */
    public Optional<MessageArchiveDTO> findByOriginalMessageId(Long messageId) {
        return archiveRepository.findByOriginalMessageId(messageId)
                .map(this::toDTOBasic);
    }
    
    /**
     * 统计归档数量
     */
    public long countArchives(Long realmId) {
        return archiveRepository.countByRealmId(realmId);
    }
    
    /**
     * 清理过期归档
     */
    @Transactional
    public int cleanupExpiredArchives(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        
        archiveRepository.markUnrecoverable(LocalDateTime.now());
        archiveRepository.deleteOldArchives(threshold);
        
        log.info("清理过期归档: threshold={}", threshold);
        return 0; // 实际应该返回删除数量
    }
    
    private MessageArchiveDTO toDTO(MessageArchive archive, String senderName) {
        return MessageArchiveDTO.builder()
                .id(archive.getId())
                .originalMessageId(archive.getOriginalMessageId())
                .content(archive.getContent())
                .renderedContent(archive.getRenderedContent())
                .senderId(archive.getSenderId())
                .streamId(archive.getStreamId())
                .topic(archive.getTopic())
                .realmId(archive.getRealmId())
                .originalDateSent(archive.getOriginalDateSent())
                .archivedAt(archive.getArchivedAt())
                .archivePolicy(archive.getArchivePolicy())
                .archivedBy(archive.getArchivedBy())
                .isRecoverable(archive.getIsRecoverable())
                .recoverUntil(archive.getRecoverUntil())
                .senderName(senderName)
                .build();
    }
    
    private MessageArchiveDTO toDTOBasic(MessageArchive archive) {
        return toDTO(archive, null);
    }
}

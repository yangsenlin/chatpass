package com.chatpass.service;

import com.chatpass.entity.MessageArchive;
import com.chatpass.repository.MessageArchiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MessageArchiveService
 * 消息归档服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageArchiveService {

    private final MessageArchiveRepository archiveRepository;

    /**
     * 归档消息
     */
    @Transactional
    public MessageArchive archiveMessage(Long realmId, Long messageId, String originalContent,
                                          String storageType, String archiveReason, LocalDateTime expireDate) {
        String archiveId = generateArchiveId();

        MessageArchive archive = MessageArchive.builder()
                .realmId(realmId)
                .archiveId(archiveId)
                .messageId(messageId)
                .originalContent(originalContent)
                .storageType(storageType)
                .archiveReason(archiveReason)
                .archiveDate(LocalDateTime.now())
                .expireDate(expireDate)
                .isDeleted(false)
                .restoreCount(0)
                .build();

        return archiveRepository.save(archive);
    }

    /**
     * 生成归档 ID
     */
    private String generateArchiveId() {
        return "ARCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 获取归档记录
     */
    public Optional<MessageArchive> getArchive(Long archiveId) {
        return archiveRepository.findById(archiveId);
    }

    /**
     * 获取归档记录（按 archiveId）
     */
    public Optional<MessageArchive> getArchiveByArchiveId(String archiveId) {
        return archiveRepository.findByArchiveId(archiveId);
    }

    /**
     * 获取消息归档
     */
    public Optional<MessageArchive> getMessageArchive(Long messageId) {
        return archiveRepository.findByMessageId(messageId);
    }

    /**
     * 获取 Realm 归档列表
     */
    public List<MessageArchive> getRealmArchivesList(Long realmId) {
        return archiveRepository.findByRealmId(realmId);
    }

    /**
     * 获取归档列表（按原因）
     */
    public List<MessageArchive> getArchivesByReason(Long realmId, String reason) {
        return archiveRepository.findByRealmIdAndArchiveReason(realmId, reason);
    }

    /**
     * 获取归档列表（按时间范围）
     */
    public List<MessageArchive> getArchivesByTimeRange(Long realmId, LocalDateTime start, LocalDateTime end) {
        return archiveRepository.findByRealmIdAndTimeRange(realmId, start, end);
    }

    /**
     * 恢复归档消息
     */
    @Transactional
    public MessageArchive restoreArchive(Long archiveId) {
        MessageArchive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("归档不存在"));

        if (archive.getIsDeleted()) {
            throw new IllegalStateException("归档已删除，无法恢复");
        }

        archiveRepository.incrementRestoreCount(archiveId, LocalDateTime.now());

        return archiveRepository.findById(archiveId).orElse(archive);
    }

    /**
     * 删除归档
     */
    @Transactional
    public void deleteArchive(Long archiveId) {
        archiveRepository.markDeleted(archiveId);
    }

    /**
     * 统计活跃归档数
     */
    public Long countActiveArchives(Long realmId) {
        return archiveRepository.countActiveArchives(realmId);
    }

    /**
     * 清理过期归档
     */
    @Transactional
    public int cleanupExpiredArchives(Long realmId) {
        List<MessageArchive> archives = archiveRepository.findByRealmId(realmId);

        int cleaned = 0;
        LocalDateTime now = LocalDateTime.now();

        for (MessageArchive archive : archives) {
            if (archive.getExpireDate() != null && archive.getExpireDate().isBefore(now)) {
                archiveRepository.markDeleted(archive.getId());
                cleaned++;
            }
        }

        log.info("Cleaned up {} expired archives for realm {}", cleaned, realmId);
        return cleaned;
    }
}
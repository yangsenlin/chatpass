package com.chatpass.service;

import com.chatpass.dto.TypingDTO;
import com.chatpass.entity.Recipient;
import com.chatpass.entity.TypingStatus;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.RecipientRepository;
import com.chatpass.repository.TypingStatusRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TypingStatusService
 * 
 * 输入提示状态管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TypingStatusService {

    private final TypingStatusRepository typingRepository;
    private final UserProfileRepository userRepository;
    private final RecipientRepository recipientRepository;

    // 默认过期时间（秒）
    private static final int DEFAULT_EXPIRE_SECONDS = 30;

    /**
     * 开始输入（私信）
     */
    @Transactional
    public TypingStatus startTypingDirect(Long userId, Long recipientId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("接收者不存在"));

        // 查找或创建输入状态
        Optional<TypingStatus> existing = typingRepository.findByUserIdAndRecipientId(userId, recipientId);
        
        if (existing.isPresent()) {
            TypingStatus status = existing.get();
            status.setLastUpdate(LocalDateTime.now());
            return typingRepository.save(status);
        }

        TypingStatus status = TypingStatus.builder()
                .user(user)
                .recipient(recipient)
                .typingType(TypingStatus.TYPE_DIRECT)
                .expiresInSeconds(DEFAULT_EXPIRE_SECONDS)
                .build();

        status = typingRepository.save(status);

        log.info("User {} started typing to recipient {}", userId, recipientId);

        return status;
    }

    /**
     * 开始输入（频道）
     */
    @Transactional
    public TypingStatus startTypingStream(Long userId, Long streamId, String topic) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 查找或创建输入状态
        Optional<TypingStatus> existing = typingRepository.findByUserIdAndStreamAndTopic(userId, streamId, topic);
        
        if (existing.isPresent()) {
            TypingStatus status = existing.get();
            status.setLastUpdate(LocalDateTime.now());
            return typingRepository.save(status);
        }

        TypingStatus status = TypingStatus.builder()
                .user(user)
                .streamId(streamId)
                .topic(topic)
                .typingType(TypingStatus.TYPE_STREAM)
                .expiresInSeconds(DEFAULT_EXPIRE_SECONDS)
                .build();

        status = typingRepository.save(status);

        log.info("User {} started typing in stream {}, topic {}", userId, streamId, topic);

        return status;
    }

    /**
     * 停止输入
     */
    @Transactional
    public void stopTyping(Long userId) {
        typingRepository.deleteByUserId(userId);

        log.info("User {} stopped typing", userId);
    }

    /**
     * 停止输入（私信）
     */
    @Transactional
    public void stopTypingDirect(Long userId, Long recipientId) {
        Optional<TypingStatus> status = typingRepository.findByUserIdAndRecipientId(userId, recipientId);
        
        status.ifPresent(typingRepository::delete);

        log.info("User {} stopped typing to recipient {}", userId, recipientId);
    }

    /**
     * 停止输入（频道）
     */
    @Transactional
    public void stopTypingStream(Long userId, Long streamId, String topic) {
        Optional<TypingStatus> status = typingRepository.findByUserIdAndStreamAndTopic(userId, streamId, topic);
        
        status.ifPresent(typingRepository::delete);

        log.info("User {} stopped typing in stream {}", userId, streamId);
    }

    /**
     * 获取正在输入的用户列表（私信）
     */
    public List<TypingStatus> getTypingUsersDirect(Long recipientId) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(DEFAULT_EXPIRE_SECONDS);
        return typingRepository.findTypingUsersForRecipient(recipientId, since)
                .stream()
                .filter(TypingStatus::isStillTyping)
                .collect(Collectors.toList());
    }

    /**
     * 获取正在输入的用户列表（频道）
     */
    public List<TypingStatus> getTypingUsersStream(Long streamId, String topic) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(DEFAULT_EXPIRE_SECONDS);
        return typingRepository.findTypingUsersForStream(streamId, topic, since)
                .stream()
                .filter(TypingStatus::isStillTyping)
                .collect(Collectors.toList());
    }

    /**
     * 统计正在输入的用户数量
     */
    public Long countTypingUsers(Long recipientId) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(DEFAULT_EXPIRE_SECONDS);
        return typingRepository.countTypingUsersForRecipient(recipientId, since);
    }

    /**
     * 清理过期的输入状态（定时任务）
     */
    @Scheduled(fixedRate = 60000) // 每 60 秒执行一次
    @Transactional
    public void cleanupExpiredStatus() {
        LocalDateTime expirationTime = LocalDateTime.now().minusSeconds(DEFAULT_EXPIRE_SECONDS * 2);
        int deleted = typingRepository.deleteExpiredStatus(expirationTime);

        if (deleted > 0) {
            log.debug("Cleaned up {} expired typing status", deleted);
        }
    }

    /**
     * 更新输入时间（保持活跃）
     */
    @Transactional
    public void keepTyping(Long userId, Long recipientId) {
        Optional<TypingStatus> status = typingRepository.findByUserIdAndRecipientId(userId, recipientId);
        
        status.ifPresent(s -> {
            s.setLastUpdate(LocalDateTime.now());
            typingRepository.save(s);
        });
    }

    /**
     * 转换为 DTO
     */
    public TypingDTO.TypingResponse toResponse(TypingStatus status) {
        TypingDTO.TypingResponse response = TypingDTO.TypingResponse.builder()
                .id(status.getId())
                .userId(status.getUser().getId())
                .userName(status.getUser().getFullName())
                .typingType(status.getTypingType())
                .lastUpdate(status.getLastUpdate().toString())
                .remainingSeconds(status.getRemainingSeconds())
                .isStillTyping(status.isStillTyping())
                .build();

        if (status.getTypingType().equals(TypingStatus.TYPE_DIRECT) && status.getRecipient() != null) {
            response.setRecipientId(status.getRecipient().getId());
        } else if (status.getTypingType().equals(TypingStatus.TYPE_STREAM)) {
            response.setStreamId(status.getStreamId());
            response.setTopic(status.getTopic());
        }

        return response;
    }

    /**
     * 获取输入状态摘要
     */
    public TypingDTO.TypingSummary getTypingSummary(Long recipientId) {
        List<TypingStatus> typingUsers = getTypingUsersDirect(recipientId);
        
        return TypingDTO.TypingSummary.builder()
                .recipientId(recipientId)
                .typingUserCount((long) typingUsers.size())
                .typingUsers(typingUsers.stream()
                        .map(s -> s.getUser().getFullName())
                        .collect(Collectors.toList()))
                .build();
    }
}
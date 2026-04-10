package com.chatpass.service;

import com.chatpass.dto.MessageStarDTO;
import com.chatpass.entity.Message;
import com.chatpass.entity.MessageStar;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.MessageRepository;
import com.chatpass.repository.MessageStarRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MessageStarService
 * 
 * 消息收藏管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageStarService {

    private final MessageStarRepository starRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;

    /**
     * 收藏消息
     */
    @Transactional
    public MessageStar starMessage(Long messageId, Long userId, String note) {
        // 检查是否已收藏
        if (starRepository.isStarred(messageId, userId)) {
            throw new IllegalStateException("消息已收藏");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));

        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        MessageStar star = MessageStar.builder()
                .message(message)
                .user(user)
                .note(note)
                .build();

        star = starRepository.save(star);

        log.info("User {} starred message {}", userId, messageId);

        return star;
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void unstarMessage(Long messageId, Long userId) {
        MessageStar star = starRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("消息未收藏"));

        starRepository.delete(star);

        log.info("User {} unstarred message {}", userId, messageId);
    }

    /**
     * 检查是否已收藏
     */
    public boolean isStarred(Long messageId, Long userId) {
        return starRepository.isStarred(messageId, userId);
    }

    /**
     * 获取用户的所有收藏
     */
    public List<MessageStar> getUserStars(Long userId) {
        return starRepository.findByUserId(userId);
    }

    /**
     * 获取用户收藏的消息 ID 列表
     */
    public List<Long> getStarredMessageIds(Long userId) {
        return starRepository.findStarredMessageIds(userId);
    }

    /**
     * 分页获取收藏
     */
    public List<MessageStar> getUserStarsPaged(Long userId, int page, int size) {
        return starRepository.findByUserIdPaged(userId, PageRequest.of(page, size));
    }

    /**
     * 统计用户收藏数量
     */
    public Long getStarCount(Long userId) {
        return starRepository.countByUserId(userId);
    }

    /**
     * 统计消息被收藏次数
     */
    public Long getMessageStarCount(Long messageId) {
        return starRepository.countByMessageId(messageId);
    }

    /**
     * 更新收藏备注
     */
    @Transactional
    public MessageStar updateNote(Long messageId, Long userId, String note) {
        MessageStar star = starRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("消息未收藏"));

        star.setNote(note);
        star = starRepository.save(star);

        log.info("Updated note for starred message {}", messageId);

        return star;
    }

    /**
     * 搜索收藏（按备注）
     */
    public List<MessageStar> searchStars(Long userId, String query) {
        return starRepository.searchByNote(userId, query);
    }

    /**
     * 获取最近收藏
     */
    public List<MessageStar> getRecentStars(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return starRepository.findRecentStars(userId, since);
    }

    /**
     * 批量收藏
     */
    @Transactional
    public List<MessageStar> starMessages(List<Long> messageIds, Long userId) {
        List<MessageStar> stars = messageIds.stream()
                .filter(id -> !starRepository.isStarred(id, userId))
                .map(id -> {
                    Message message = messageRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("消息不存在: " + id));
                    UserProfile user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
                    return MessageStar.builder()
                            .message(message)
                            .user(user)
                            .build();
                })
                .collect(Collectors.toList());

        stars = starRepository.saveAll(stars);

        log.info("User {} starred {} messages", userId, stars.size());

        return stars;
    }

    /**
     * 批量取消收藏
     */
    @Transactional
    public void unstarMessages(List<Long> messageIds, Long userId) {
        messageIds.forEach(id -> {
            starRepository.findByMessageIdAndUserId(id, userId)
                    .ifPresent(starRepository::delete);
        });

        log.info("User {} unstarred {} messages", userId, messageIds.size());
    }

    /**
     * 转换为 DTO
     */
    public MessageStarDTO.StarResponse toResponse(MessageStar star) {
        return MessageStarDTO.StarResponse.builder()
                .id(star.getId())
                .messageId(star.getMessage().getId())
                .userId(star.getUser().getId())
                .userName(star.getUser().getFullName())
                .starredTime(star.getStarredTime().toString())
                .note(star.getNote())
                .build();
    }

    /**
     * 获取收藏摘要
     */
    public MessageStarDTO.StarSummary getStarSummary(Long userId) {
        Long count = getStarCount(userId);
        List<MessageStar> recent = getUserStarsPaged(userId, 0, 5);

        return MessageStarDTO.StarSummary.builder()
                .userId(userId)
                .totalStars(count)
                .recentStarIds(recent.stream()
                        .map(s -> s.getMessage().getId())
                        .collect(Collectors.toList()))
                .build();
    }
}
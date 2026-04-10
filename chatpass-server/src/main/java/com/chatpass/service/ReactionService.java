package com.chatpass.service;

import com.chatpass.dto.ReactionDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reaction 服务
 * 
 * 消息表情反应管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 添加表情反应
     */
    @Transactional
    public ReactionDTO.Response addReaction(Long userId, Long realmId, ReactionDTO.AddRequest request) {
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message", request.getMessageId()));

        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // 检查是否已存在
        if (reactionRepository.existsByUserIdAndMessageIdAndEmojiCode(userId, request.getMessageId(), request.getEmojiCode())) {
            // 已存在，返回现有的
            Reaction existing = reactionRepository.findByUserIdAndMessageIdAndEmojiCode(
                    userId, request.getMessageId(), request.getEmojiCode()).orElse(null);
            if (existing != null) {
                return toResponse(existing);
            }
        }

        Reaction reaction = Reaction.builder()
                .message(message)
                .user(user)
                .realm(realm)
                .emojiCode(request.getEmojiCode())
                .emojiName(request.getEmojiName() != null ? request.getEmojiName() : request.getEmojiCode())
                .emojiType(request.getEmojiType() != null ? request.getEmojiType() : "unicode")
                .build();

        reaction = reactionRepository.save(reaction);

        // 发布 WebSocket 事件
        // eventPublisher.publishEvent(new ReactionEvent(reaction, "add"));

        log.info("User {} added reaction {} to message {}", userId, request.getEmojiCode(), request.getMessageId());

        return toResponse(reaction);
    }

    /**
     * 移除表情反应
     */
    @Transactional
    public void removeReaction(Long userId, Long messageId, String emojiCode) {
        reactionRepository.deleteByUserIdAndMessageIdAndEmojiCode(userId, messageId, emojiCode);

        log.info("User {} removed reaction {} from message {}", userId, emojiCode, messageId);
    }

    /**
     * 获取消息的所有反应（聚合）
     */
    @Transactional(readOnly = true)
    public ReactionDTO.ListResponse getMessageReactions(Long messageId) {
        List<Reaction> reactions = reactionRepository.findByMessageIdOrderByEmoji(messageId);

        // 按 emoji 聚合
        Map<String, List<Reaction>> grouped = reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getEmojiCode));

        List<ReactionDTO.AggregatedResponse> aggregated = new ArrayList<>();

        for (Map.Entry<String, List<Reaction>> entry : grouped.entrySet()) {
            String emojiCode = entry.getKey();
            List<Reaction> emojiReactions = entry.getValue();

            List<ReactionDTO.UserInfo> users = emojiReactions.stream()
                    .map(r -> ReactionDTO.UserInfo.builder()
                            .id(r.getUser().getId())
                            .fullName(r.getUser().getFullName())
                            .email(r.getUser().getEmail())
                            .build())
                    .collect(Collectors.toList());

            aggregated.add(ReactionDTO.AggregatedResponse.builder()
                    .emojiCode(emojiCode)
                    .emojiName(emojiReactions.get(0).getEmojiName())
                    .count(emojiReactions.size())
                    .users(users)
                    .build());
        }

        return ReactionDTO.ListResponse.builder()
                .messageId(messageId)
                .reactions(aggregated)
                .build();
    }

    /**
     * 批量获取多条消息的反应
     */
    @Transactional(readOnly = true)
    public Map<Long, ReactionDTO.ListResponse> getReactionsForMessages(List<Long> messageIds) {
        Map<Long, ReactionDTO.ListResponse> result = new HashMap<>();

        for (Long messageId : messageIds) {
            result.put(messageId, getMessageReactions(messageId));
        }

        return result;
    }

    /**
     * 检查用户是否已对某消息添加了特定表情
     */
    @Transactional(readOnly = true)
    public boolean hasReacted(Long userId, Long messageId, String emojiCode) {
        return reactionRepository.existsByUserIdAndMessageIdAndEmojiCode(userId, messageId, emojiCode);
    }

    /**
     * 获取用户在某消息上的所有反应
     */
    @Transactional(readOnly = true)
    public List<ReactionDTO.Response> getUserReactionsOnMessage(Long userId, Long messageId) {
        return reactionRepository.findByMessageId(messageId).stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReactionDTO.Response toResponse(Reaction reaction) {
        return ReactionDTO.Response.builder()
                .id(reaction.getId())
                .messageId(reaction.getMessage().getId())
                .userId(reaction.getUser().getId())
                .userName(reaction.getUser().getFullName())
                .emojiCode(reaction.getEmojiCode())
                .emojiName(reaction.getEmojiName())
                .emojiType(reaction.getEmojiType())
                .dateCreated(reaction.getDateCreated())
                .build();
    }
}
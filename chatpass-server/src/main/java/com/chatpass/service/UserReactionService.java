package com.chatpass.service;

import com.chatpass.dto.UserReactionDTO;
import com.chatpass.entity.UserReaction;
import com.chatpass.repository.UserReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户反应服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserReactionService {
    
    private final UserReactionRepository reactionRepository;
    
    /**
     * 添加反应
     */
    @Transactional
    public UserReactionDTO.ReactionInfo addReaction(Long messageId, Long userId, 
                                                      String reactionType, Long realmId) {
        
        // 检查是否已添加相同反应
        if (reactionRepository.existsByMessageIdAndUserIdAndReactionType(messageId, userId, reactionType)) {
            throw new IllegalStateException("已添加该反应");
        }
        
        UserReaction reaction = UserReaction.builder()
                .messageId(messageId)
                .userId(userId)
                .reactionType(reactionType)
                .realmId(realmId)
                .build();
        
        reaction = reactionRepository.save(reaction);
        log.info("添加反应: messageId={}, userId={}, reaction={}", messageId, userId, reactionType);
        
        return toReactionInfo(reaction);
    }
    
    /**
     * 移除反应
     */
    @Transactional
    public void removeReaction(Long messageId, Long userId, String reactionType) {
        if (reactionType != null) {
            reactionRepository.deleteByMessageIdUserIdAndReactionType(messageId, userId, reactionType);
        } else {
            reactionRepository.deleteByMessageIdAndUserId(messageId, userId);
        }
        log.info("移除反应: messageId={}, userId={}", messageId, userId);
    }
    
    /**
     * 获取消息的所有反应
     */
    public List<UserReactionDTO.ReactionInfo> getMessageReactions(Long messageId) {
        return reactionRepository.findByMessageIdOrderByReactedAtAsc(messageId)
                .stream()
                .map(this::toReactionInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取消息的反应统计
     */
    public UserReactionDTO.ReactionStats getReactionStats(Long messageId) {
        List<Object[]> stats = reactionRepository.getReactionStats(messageId);
        
        Map<String, Long> reactionCounts = new HashMap<>();
        Map<String, List<Long>> reactionUsers = new HashMap<>();
        long totalReactions = 0;
        
        for (Object[] row : stats) {
            String reactionType = (String) row[0];
            Long count = (Long) row[1];
            
            reactionCounts.put(reactionType, count);
            totalReactions += count;
            
            List<Long> users = reactionRepository.findUsersByReaction(messageId, reactionType);
            reactionUsers.put(reactionType, users);
        }
        
        return UserReactionDTO.ReactionStats.builder()
                .messageId(messageId)
                .reactionCounts(reactionCounts)
                .reactionUsers(reactionUsers)
                .totalReactions(totalReactions)
                .build();
    }
    
    /**
     * 获取用户的反应
     */
    public Optional<UserReactionDTO.ReactionInfo> getUserReaction(Long messageId, Long userId) {
        return reactionRepository.findByMessageIdAndUserId(messageId, userId)
                .map(this::toReactionInfo);
    }
    
    /**
     * 获取用户的所有反应
     */
    public List<UserReactionDTO.ReactionInfo> getUserReactions(Long userId) {
        return reactionRepository.findByUserIdOrderByReactedAtDesc(userId)
                .stream()
                .map(this::toReactionInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否已反应
     */
    public boolean hasReaction(Long messageId, Long userId) {
        return reactionRepository.existsByMessageIdAndUserId(messageId, userId);
    }
    
    /**
     * 检查特定反应是否存在
     */
    public boolean hasSpecificReaction(Long messageId, Long userId, String reactionType) {
        return reactionRepository.existsByMessageIdAndUserIdAndReactionType(messageId, userId, reactionType);
    }
    
    /**
     * 统计反应数量
     */
    public long countReactions(Long messageId) {
        return reactionRepository.countByMessageId(messageId);
    }
    
    /**
     * 统计特定反应数量
     */
    public long countReactionsByType(Long messageId, String reactionType) {
        return reactionRepository.countByMessageIdAndReactionType(messageId, reactionType);
    }
    
    /**
     * 获取特定反应的用户
     */
    public List<Long> getReactionUsers(Long messageId, String reactionType) {
        return reactionRepository.findUsersByReaction(messageId, reactionType);
    }
    
    private UserReactionDTO.ReactionInfo toReactionInfo(UserReaction reaction) {
        return UserReactionDTO.ReactionInfo.builder()
                .id(reaction.getId())
                .messageId(reaction.getMessageId())
                .userId(reaction.getUserId())
                .reactionType(reaction.getReactionType())
                .reactedAt(reaction.getReactedAt())
                .realmId(reaction.getRealmId())
                .build();
    }
}

package com.chatpass.service;

import com.chatpass.entity.*;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * StarredMessageService - 消息星标服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StarredMessageService {

    private final UserMessageRepository userMessageRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;

    /**
     * 星标消息
     */
    @Transactional
    public Long starMessage(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 检查是否已存在
        Optional<UserMessage> existing = userMessageRepository.findByUserProfileIdAndMessageId(userId, messageId);
        
        if (existing.isPresent()) {
            // 更新flags
            UserMessage um = existing.get();
            um.setFlags(um.getFlags() | UserMessage.FLAG_STARRED);
            userMessageRepository.save(um);
        } else {
            // 创建新的
            UserMessage userMessage = UserMessage.builder()
                    .userProfile(user)
                    .message(message)
                    .flags(UserMessage.FLAG_STARRED)
                    .build();
            userMessageRepository.save(userMessage);
        }
        
        log.info("User {} starred message {}", userId, messageId);
        return messageId;
    }

    /**
     * 取消星标
     */
    @Transactional
    public void unstarMessage(Long userId, Long messageId) {
        Optional<UserMessage> um = userMessageRepository.findByUserProfileIdAndMessageId(userId, messageId);
        
        if (um.isPresent()) {
            UserMessage userMessage = um.get();
            // 清除星标flag
            userMessage.setFlags(userMessage.getFlags() & ~UserMessage.FLAG_STARRED);
            
            if (userMessage.getFlags() == 0) {
                // 没有其他flag，删除记录
                userMessageRepository.delete(userMessage);
            } else {
                userMessageRepository.save(userMessage);
            }
        }
        
        log.info("User {} unstarred message {}", userId, messageId);
    }

    /**
     * 获取用户的所有星标消息
     */
    @Transactional(readOnly = true)
    public List<Long> getStarredMessages(Long userId) {
        List<UserMessage> messages = userMessageRepository.findByUserProfileId(userId);
        
        return messages.stream()
                .filter(um -> (um.getFlags() & UserMessage.FLAG_STARRED) != 0)
                .map(um -> um.getMessage().getId())
                .collect(Collectors.toList());
    }

    /**
     * 获取星标统计
     */
    @Transactional(readOnly = true)
    public Integer getStarredCount(Long userId) {
        List<UserMessage> messages = userMessageRepository.findByUserProfileId(userId);
        
        return (int) messages.stream()
                .filter(um -> (um.getFlags() & UserMessage.FLAG_STARRED) != 0)
                .count();
    }

    /**
     * 检查消息是否已星标
     */
    @Transactional(readOnly = true)
    public boolean isStarred(Long userId, Long messageId) {
        Optional<UserMessage> um = userMessageRepository.findByUserProfileIdAndMessageId(userId, messageId);
        
        return um.isPresent() && (um.get().getFlags() & UserMessage.FLAG_STARRED) != 0;
    }
}
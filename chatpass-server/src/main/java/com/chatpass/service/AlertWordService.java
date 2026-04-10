package com.chatpass.service;

import com.chatpass.entity.UserProfile;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Alert Word 服务
 * 
 * 用户自定义关键词提醒
 * 当消息包含关键词时自动设置 FLAG_HAS_ALERT_WORD
 */
@Service
@RequiredArgsConstructor
public class AlertWordService {

    private final UserProfileRepository userRepository;

    /**
     * 检测消息是否包含用户的 Alert Words
     * 
     * @param userId 用户 ID
     * @param content 消息内容
     * @return 是否包含 Alert Word
     */
    public boolean containsAlertWord(Long userId, String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        // 获取用户的 Alert Words
        Set<String> alertWords = getUserAlertWords(userId);
        if (alertWords.isEmpty()) {
            return false;
        }

        // 检测关键词（忽略大小写）
        String lowerContent = content.toLowerCase();
        for (String word : alertWords) {
            if (lowerContent.contains(word.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检测消息中包含的所有 Alert Words
     */
    public List<String> findAlertWords(Long userId, String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }

        Set<String> alertWords = getUserAlertWords(userId);
        String lowerContent = content.toLowerCase();

        return alertWords.stream()
                .filter(word -> lowerContent.contains(word.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * 为所有用户检测 Alert Words
     * 返回包含关键词的用户 ID 列表
     */
    public List<Long> findUsersWithAlertWord(Long realmId, String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }

        List<UserProfile> users = userRepository.findByRealmIdAndIsActiveTrue(realmId);
        String lowerContent = content.toLowerCase();

        return users.stream()
                .filter(user -> {
                    Set<String> alertWords = getUserAlertWords(user.getId());
                    for (String word : alertWords) {
                        if (lowerContent.contains(word.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(UserProfile::getId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的 Alert Words
     * 
     * TODO: 从数据库或用户配置中获取
     * 目前返回空集合，等待实现用户配置功能
     */
    private Set<String> getUserAlertWords(Long userId) {
        // TODO: 实现 Alert Words 存储
        // 可能存储在 UserProfile.alertWords (JSON)
        // 或独立的 AlertWord 实体
        
        return Set.of();
    }

    /**
     * 设置用户的 Alert Words
     */
    public void setAlertWords(Long userId, Set<String> words) {
        // TODO: 实现存储逻辑
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // user.setAlertWords(words);
        // userRepository.save(user);
    }

    /**
     * 添加 Alert Word
     */
    public void addAlertWord(Long userId, String word) {
        // TODO: 实现添加逻辑
    }

    /**
     * 删除 Alert Word
     */
    public void removeAlertWord(Long userId, String word) {
        // TODO: 实现删除逻辑
    }
}
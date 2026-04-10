package com.chatpass.service;

import com.chatpass.dto.AlertWordDTO;
import com.chatpass.entity.AlertWord;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.AlertWordRepository;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AlertWord 服务
 * 
 * 用户自定义关键词提醒完整实现
 */
@Service
@RequiredArgsConstructor
public class AlertWordService {

    private final AlertWordRepository alertWordRepository;
    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;

    /**
     * 获取用户的所有 Alert Words
     */
    @Transactional(readOnly = true)
    public AlertWordDTO.ListResponse getUserAlertWords(Long userId) {
        List<AlertWord> words = alertWordRepository.findByUserId(userId);
        
        List<AlertWordDTO.Response> responses = words.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return AlertWordDTO.ListResponse.builder()
                .alertWords(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取用户的活跃 Alert Words（仅词语）
     */
    @Transactional(readOnly = true)
    public Set<String> getActiveWords(Long userId) {
        return alertWordRepository.findWordsByUserId(userId);
    }

    /**
     * 添加 Alert Word
     */
    @Transactional
    public AlertWordDTO.Response addAlertWord(Long userId, Long realmId, AlertWordDTO.CreateRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // 检查是否已存在
        String word = request.getWord().toLowerCase().trim();
        if (alertWordRepository.existsByUserIdAndWord(userId, word)) {
            // 已存在，返回现有的
            AlertWord existing = alertWordRepository.findByUserIdAndWord(userId, word).orElse(null);
            if (existing != null) {
                return toResponse(existing);
            }
        }

        AlertWord alertWord = AlertWord.builder()
                .user(user)
                .realm(realm)
                .word(word)
                .matchMode(request.getMatchMode() != null ? request.getMatchMode() : AlertWord.MODE_CONTAINS)
                .notifyEmail(request.getNotifyEmail() != null ? request.getNotifyEmail() : false)
                .notifyPush(request.getNotifyPush() != null ? request.getNotifyPush() : true)
                .notifyDesktop(request.getNotifyDesktop() != null ? request.getNotifyDesktop() : true)
                .isActive(true)
                .build();

        alertWord = alertWordRepository.save(alertWord);
        return toResponse(alertWord);
    }

    /**
     * 批量添加 Alert Words
     */
    @Transactional
    public AlertWordDTO.ListResponse addAlertWords(Long userId, Long realmId, AlertWordDTO.BatchRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        List<AlertWord> added = new java.util.ArrayList<>();
        
        for (String word : request.getWords()) {
            String normalizedWord = word.toLowerCase().trim();
            
            // 跳过已存在的
            if (alertWordRepository.existsByUserIdAndWord(userId, normalizedWord)) {
                continue;
            }

            AlertWord alertWord = AlertWord.builder()
                    .user(user)
                    .realm(realm)
                    .word(normalizedWord)
                    .matchMode(AlertWord.MODE_CONTAINS)
                    .notifyPush(true)
                    .notifyDesktop(true)
                    .isActive(true)
                    .build();

            added.add(alertWordRepository.save(alertWord));
        }

        List<AlertWordDTO.Response> responses = added.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return AlertWordDTO.ListResponse.builder()
                .alertWords(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 更新 Alert Word
     */
    @Transactional
    public AlertWordDTO.Response updateAlertWord(Long userId, Long alertWordId, AlertWordDTO.UpdateRequest request) {
        AlertWord alertWord = alertWordRepository.findById(alertWordId)
                .orElseThrow(() -> new ResourceNotFoundException("AlertWord", alertWordId));

        if (!alertWord.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only modify your own alert words");
        }

        if (request.getIsActive() != null) {
            alertWord.setIsActive(request.getIsActive());
        }
        if (request.getMatchMode() != null) {
            alertWord.setMatchMode(request.getMatchMode());
        }
        if (request.getNotifyEmail() != null) {
            alertWord.setNotifyEmail(request.getNotifyEmail());
        }
        if (request.getNotifyPush() != null) {
            alertWord.setNotifyPush(request.getNotifyPush());
        }
        if (request.getNotifyDesktop() != null) {
            alertWord.setNotifyDesktop(request.getNotifyDesktop());
        }

        alertWord = alertWordRepository.save(alertWord);
        return toResponse(alertWord);
    }

    /**
     * 删除 Alert Word
     */
    @Transactional
    public void removeAlertWord(Long userId, Long alertWordId) {
        AlertWord alertWord = alertWordRepository.findById(alertWordId)
                .orElseThrow(() -> new ResourceNotFoundException("AlertWord", alertWordId));

        if (!alertWord.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own alert words");
        }

        alertWordRepository.delete(alertWord);
    }

    /**
     * 通过词语删除
     */
    @Transactional
    public void removeAlertWordByText(Long userId, String word) {
        alertWordRepository.deleteByUserIdAndWord(userId, word.toLowerCase().trim());
    }

    /**
     * 检测消息是否包含用户的 Alert Words
     */
    @Transactional(readOnly = true)
    public boolean containsAlertWord(Long userId, String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        Set<String> alertWords = getActiveWords(userId);
        if (alertWords.isEmpty()) {
            return false;
        }

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
    @Transactional(readOnly = true)
    public List<String> findMatchedWords(Long userId, String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }

        Set<String> alertWords = getActiveWords(userId);
        String lowerContent = content.toLowerCase();

        return alertWords.stream()
                .filter(word -> lowerContent.contains(word.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * 为 Realm 中所有用户检测 Alert Words
     * 返回匹配的用户 ID 列表
     */
    @Transactional(readOnly = true)
    public List<Long> findUsersWithAlertWord(Long realmId, String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }

        List<AlertWord> allActive = alertWordRepository.findActiveByRealmId(realmId);
        String lowerContent = content.toLowerCase();

        return allActive.stream()
                .filter(aw -> lowerContent.contains(aw.getWord().toLowerCase()))
                .map(aw -> aw.getUser().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应
     */
    private AlertWordDTO.Response toResponse(AlertWord alertWord) {
        return AlertWordDTO.Response.builder()
                .id(alertWord.getId())
                .word(alertWord.getWord())
                .isActive(alertWord.getIsActive())
                .matchMode(alertWord.getMatchMode())
                .notifyEmail(alertWord.getNotifyEmail())
                .notifyPush(alertWord.getNotifyPush())
                .notifyDesktop(alertWord.getNotifyDesktop())
                .dateCreated(alertWord.getDateCreated())
                .build();
    }
}
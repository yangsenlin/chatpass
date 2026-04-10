package com.chatpass.service;

import com.chatpass.entity.*;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Draft 服务
 * 
 * 消息草稿管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DraftService {

    private final DraftRepository draftRepository;
    private final UserProfileRepository userRepository;
    private final RecipientRepository recipientRepository;

    /**
     * 保存草稿
     */
    @Transactional
    public Draft saveDraft(Long userId, Long recipientId, String topic, String content) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Recipient recipient = null;
        if (recipientId != null) {
            recipient = recipientRepository.findById(recipientId).orElse(null);
        }

        // 查找现有草稿
        Optional<Draft> existing = draftRepository.findByUserIdAndRecipientIdAndTopic(userId, recipientId, topic);

        Draft draft;
        if (existing.isPresent()) {
            draft = existing.get();
            draft.setContent(content);
            draft.setLastEditTime(LocalDateTime.now());
        } else {
            draft = Draft.builder()
                    .user(user)
                    .recipient(recipient)
                    .topic(topic)
                    .content(content)
                    .lastEditTime(LocalDateTime.now())
                    .build();
        }

        draft = draftRepository.save(draft);
        log.debug("Saved draft for user {} in recipient {} topic {}", userId, recipientId, topic);

        return draft;
    }

    /**
     * 获取用户的草稿
     */
    @Transactional(readOnly = true)
    public List<Draft> getUserDrafts(Long userId) {
        return draftRepository.findByUserIdOrderByLastUpdatedDesc(userId);
    }

    /**
     * 获取特定草稿
     */
    @Transactional(readOnly = true)
    public Optional<Draft> getDraft(Long userId, Long recipientId, String topic) {
        return draftRepository.findByUserIdAndRecipientIdAndTopic(userId, recipientId, topic);
    }

    /**
     * 删除草稿
     */
    @Transactional
    public void deleteDraft(Long userId, Long recipientId, String topic) {
        draftRepository.deleteByUserIdAndRecipientIdAndTopic(userId, recipientId, topic);
        log.debug("Deleted draft for user {} in recipient {} topic {}", userId, recipientId, topic);
    }

    /**
     * 清空用户所有草稿
     */
    @Transactional
    public void clearAllDrafts(Long userId) {
        List<Draft> drafts = draftRepository.findByUserId(userId);
        draftRepository.deleteAll(drafts);
        log.info("Cleared {} drafts for user {}", drafts.size(), userId);
    }
}
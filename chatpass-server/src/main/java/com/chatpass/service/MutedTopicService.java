package com.chatpass.service;

import com.chatpass.entity.MutedTopic;
import com.chatpass.repository.MutedTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 静音话题服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MutedTopicService {

    private final MutedTopicRepository mutedTopicRepository;

    /**
     * 静音话题
     */
    @Transactional
    public void muteTopic(Long userId, Long streamId, String topicName) {
        if (!mutedTopicRepository.existsByUserIdAndStreamIdAndTopicName(userId, streamId, topicName)) {
            MutedTopic muted = MutedTopic.builder()
                    .user(com.chatpass.entity.UserProfile.builder().id(userId).build())
                    .streamId(streamId)
                    .topicName(topicName)
                    .build();
            mutedTopicRepository.save(muted);
            log.debug("User {} muted topic {} in stream {}", userId, topicName, streamId);
        }
    }

    /**
     * 取消静音
     */
    @Transactional
    public void unmuteTopic(Long userId, Long streamId, String topicName) {
        mutedTopicRepository.deleteByUserIdAndStreamIdAndTopicName(userId, streamId, topicName);
        log.debug("User {} unmuted topic {} in stream {}", userId, topicName, streamId);
    }

    /**
     * 获取用户静音话题
     */
    @Transactional(readOnly = true)
    public List<MutedTopic> getUserMutedTopics(Long userId) {
        return mutedTopicRepository.findByUserId(userId);
    }

    /**
     * 检查话题是否静音
     */
    @Transactional(readOnly = true)
    public boolean isTopicMuted(Long userId, Long streamId, String topicName) {
        return mutedTopicRepository.existsByUserIdAndStreamIdAndTopicName(userId, streamId, topicName);
    }
}
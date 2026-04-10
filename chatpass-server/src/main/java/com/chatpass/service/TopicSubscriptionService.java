package com.chatpass.service;

import com.chatpass.dto.TopicDTO;
import com.chatpass.entity.TopicSubscription;
import com.chatpass.repository.TopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TopicSubscriptionService
 * 
 * 话题订阅管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicSubscriptionService {

    private final TopicSubscriptionRepository repository;
    private final AuditLogService auditLogService;

    /**
     * 订阅话题
     */
    @Transactional
    public TopicSubscription subscribe(Long userId, Long streamId, String topicName, 
                                        String subscriptionType, 
                                        Boolean desktopNotifications,
                                        Boolean emailNotifications,
                                        Boolean pushNotifications) {
        // 检查是否已订阅
        Optional<TopicSubscription> existing = repository.findByUserStreamTopic(userId, streamId, topicName);
        
        if (existing.isPresent()) {
            // 更新订阅
            TopicSubscription sub = existing.get();
            sub.setSubscriptionType(subscriptionType != null ? subscriptionType : TopicSubscription.TYPE_NOTIFY);
            if (desktopNotifications != null) sub.setDesktopNotifications(desktopNotifications);
            if (emailNotifications != null) sub.setEmailNotifications(emailNotifications);
            if (pushNotifications != null) sub.setPushNotifications(pushNotifications);
            sub.setLastUpdated(LocalDateTime.now());
            
            sub = repository.save(sub);
            
            log.info("Topic subscription updated: {} for user {}", topicName, userId);
            
            return sub;
        }

        // 创建新订阅
        TopicSubscription sub = TopicSubscription.builder()
                .userId(userId)
                .streamId(streamId)
                .topicName(topicName)
                .subscriptionType(subscriptionType != null ? subscriptionType : TopicSubscription.TYPE_NOTIFY)
                .desktopNotifications(desktopNotifications != null ? desktopNotifications : true)
                .emailNotifications(emailNotifications != null ? emailNotifications : false)
                .pushNotifications(pushNotifications != null ? pushNotifications : true)
                .soundNotifications(true)
                .build();

        sub = repository.save(sub);

        auditLogService.logCreate(userId, "TOPIC_SUBSCRIPTION", sub.getId(), sub);

        log.info("Topic subscription created: {} for user {}", topicName, userId);

        return sub;
    }

    /**
     * 取消订阅
     */
    @Transactional
    public void unsubscribe(Long userId, Long streamId, String topicName) {
        Optional<TopicSubscription> sub = repository.findByUserStreamTopic(userId, streamId, topicName);
        
        if (sub.isPresent()) {
            repository.delete(sub.get());
            
            auditLogService.logDelete(userId, "TOPIC_SUBSCRIPTION", sub.get().getId(), sub.get());
            
            log.info("Topic unsubscribed: {} for user {}", topicName, userId);
        }
    }

    /**
     * 静音话题
     */
    @Transactional
    public TopicSubscription muteTopic(Long userId, Long streamId, String topicName) {
        return subscribe(userId, streamId, topicName, TopicSubscription.TYPE_MUTE, false, false, false);
    }

    /**
     * 取消静音
     */
    @Transactional
    public TopicSubscription unmuteTopic(Long userId, Long streamId, String topicName) {
        return subscribe(userId, streamId, topicName, TopicSubscription.TYPE_NOTIFY, true, false, true);
    }

    /**
     * 设置仅提及通知
     */
    @Transactional
    public TopicSubscription setMentionOnly(Long userId, Long streamId, String topicName) {
        return subscribe(userId, streamId, topicName, TopicSubscription.TYPE_MENTION_ONLY, false, false, false);
    }

    /**
     * 获取用户订阅列表
     */
    public List<TopicSubscription> getUserSubscriptions(Long userId) {
        return repository.findByUserId(userId);
    }

    /**
     * 获取 Stream 的订阅列表
     */
    public List<TopicSubscription> getStreamSubscriptions(Long streamId) {
        return repository.findByStreamId(streamId);
    }

    /**
     * 获取特定订阅
     */
    public Optional<TopicSubscription> getSubscription(Long userId, Long streamId, String topicName) {
        return repository.findByUserStreamTopic(userId, streamId, topicName);
    }

    /**
     * 获取 Topic 订阅者
     */
    public List<TopicSubscription> getTopicSubscribers(Long streamId, String topicName) {
        return repository.findTopicSubscribers(streamId, topicName);
    }

    /**
     * 获取静音列表
     */
    public List<TopicSubscription> getMutedTopics(Long userId) {
        return repository.findMutedSubscriptions(userId);
    }

    /**
     * 获取通知列表
     */
    public List<TopicSubscription> getNotifyTopics(Long userId) {
        return repository.findNotifySubscriptions(userId);
    }

    /**
     * 统计订阅数
     */
    public Long countSubscriptions(Long userId) {
        return repository.countByUserId(userId);
    }

    /**
     * 统计 Topic 订阅数
     */
    public Long countTopicSubscribers(Long streamId, String topicName) {
        return repository.countByStreamIdAndTopicName(streamId, topicName);
    }

    /**
     * 检查是否应该通知用户
     */
    public boolean shouldNotifyUser(Long userId, Long streamId, String topicName) {
        Optional<TopicSubscription> sub = repository.findByUserStreamTopic(userId, streamId, topicName);
        
        if (!sub.isPresent()) {
            // 未订阅默认通知
            return true;
        }
        
        return sub.get().shouldNotify();
    }

    /**
     * 批量订阅
     */
    @Transactional
    public List<TopicSubscription> batchSubscribe(Long userId, List<TopicDTO.SubscribeRequest> requests) {
        return requests.stream()
                .map(r -> subscribe(userId, r.getStreamId(), r.getTopicName(), 
                        r.getSubscriptionType(), r.getDesktopNotifications(), 
                        r.getEmailNotifications(), r.getPushNotifications()))
                .collect(Collectors.toList());
    }

    /**
     * 转换为 DTO
     */
    public TopicDTO.SubscriptionResponse toResponse(TopicSubscription sub) {
        return TopicDTO.SubscriptionResponse.builder()
                .id(sub.getId())
                .userId(sub.getUserId())
                .streamId(sub.getStreamId())
                .topicName(sub.getTopicName())
                .subscriptionType(sub.getSubscriptionType())
                .desktopNotifications(sub.getDesktopNotifications())
                .emailNotifications(sub.getEmailNotifications())
                .pushNotifications(sub.getPushNotifications())
                .soundNotifications(sub.getSoundNotifications())
                .isMuted(sub.isMuted())
                .dateSubscribed(sub.getDateSubscribed().toString())
                .build();
    }
}
package com.chatpass.service;

import com.chatpass.dto.TopicSubscriptionDTO;
import com.chatpass.entity.TopicSubscription;
import com.chatpass.repository.TopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 话题订阅服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicSubscriptionService {
    
    private final TopicSubscriptionRepository subscriptionRepository;
    
    /**
     * 订阅话题
     */
    @Transactional
    public TopicSubscriptionDTO subscribe(Long userId, Long streamId, String topic, Long realmId) {
        
        // 检查是否已订阅
        if (subscriptionRepository.existsByUserIdAndStreamIdAndTopic(userId, streamId, topic)) {
            throw new IllegalStateException("已订阅该话题");
        }
        
        TopicSubscription subscription = TopicSubscription.builder()
                .userId(userId)
                .streamId(streamId)
                .topic(topic)
                .realmId(realmId)
                .isMuted(false)
                .notificationSettings("all")
                .build();
        
        subscription = subscriptionRepository.save(subscription);
        log.info("订阅话题: userId={}, streamId={}, topic={}", userId, streamId, topic);
        
        return toDTO(subscription);
    }
    
    /**
     * 取消订阅
     */
    @Transactional
    public void unsubscribe(Long userId, Long streamId, String topic) {
        subscriptionRepository.deleteByUserIdAndStreamIdAndTopic(userId, streamId, topic);
        log.info("取消订阅话题: userId={}, streamId={}, topic={}", userId, streamId, topic);
    }
    
    /**
     * 获取用户的所有订阅
     */
    public List<TopicSubscriptionDTO> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdOrderBySubscribedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户在Stream的订阅
     */
    public List<TopicSubscriptionDTO> getUserStreamSubscriptions(Long userId, Long streamId) {
        return subscriptionRepository.findByUserIdAndStreamId(userId, streamId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取话题的订阅者
     */
    public List<TopicSubscriptionDTO> getTopicSubscribers(Long streamId, String topic) {
        return subscriptionRepository.findByStreamIdAndTopic(streamId, topic)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取活跃订阅者（未静音）
     */
    public List<Long> getActiveSubscriberIds(Long streamId, String topic) {
        return subscriptionRepository.findActiveSubscribers(streamId, topic)
                .stream()
                .map(TopicSubscription::getUserId)
                .collect(Collectors.toList());
    }
    
    /**
     * 设置静音状态
     */
    @Transactional
    public void setMuted(Long userId, Long streamId, String topic, boolean muted) {
        TopicSubscription subscription = subscriptionRepository.findByUserIdAndStreamIdAndTopic(userId, streamId, topic)
                .orElseThrow(() -> new IllegalArgumentException("订阅不存在"));
        
        subscription.setIsMuted(muted);
        subscriptionRepository.save(subscription);
        log.info("设置话题静音: userId={}, topic={}, muted={}", userId, topic, muted);
    }
    
    /**
     * 设置通知配置
     */
    @Transactional
    public void setNotificationSettings(Long userId, Long streamId, String topic, String settings) {
        TopicSubscription subscription = subscriptionRepository.findByUserIdAndStreamIdAndTopic(userId, streamId, topic)
                .orElseThrow(() -> new IllegalArgumentException("订阅不存在"));
        
        subscription.setNotificationSettings(settings);
        subscriptionRepository.save(subscription);
        log.info("设置通知配置: userId={}, topic={}, settings={}", userId, topic, settings);
    }
    
    /**
     * 检查是否订阅
     */
    public boolean isSubscribed(Long userId, Long streamId, String topic) {
        return subscriptionRepository.existsByUserIdAndStreamIdAndTopic(userId, streamId, topic);
    }
    
    /**
     * 获取订阅详情
     */
    public Optional<TopicSubscriptionDTO> getSubscription(Long userId, Long streamId, String topic) {
        return subscriptionRepository.findByUserIdAndStreamIdAndTopic(userId, streamId, topic)
                .map(this::toDTO);
    }
    
    /**
     * 统计订阅者数量
     */
    public long getSubscriberCount(Long streamId, String topic) {
        return subscriptionRepository.countByStreamIdAndTopic(streamId, topic);
    }
    
    private TopicSubscriptionDTO toDTO(TopicSubscription subscription) {
        return TopicSubscriptionDTO.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .streamId(subscription.getStreamId())
                .topic(subscription.getTopic())
                .realmId(subscription.getRealmId())
                .isMuted(subscription.getIsMuted())
                .notificationSettings(subscription.getNotificationSettings())
                .subscribedAt(subscription.getSubscribedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}

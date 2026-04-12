package com.chatpass.controller.api.v1;

import com.chatpass.dto.TopicSubscriptionDTO;
import com.chatpass.service.TopicSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 话题订阅控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TopicSubscriptionController {
    
    private final TopicSubscriptionService subscriptionService;
    
    /**
     * 订阅话题
     */
    @PostMapping("/users/{userId}/topic_subscriptions")
    public ResponseEntity<TopicSubscriptionDTO> subscribe(
            @PathVariable Long userId,
            @RequestParam Long streamId,
            @RequestParam String topic,
            @RequestParam(required = false) Long realmId) {
        
        TopicSubscriptionDTO subscription = subscriptionService.subscribe(userId, streamId, topic, realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }
    
    /**
     * 取消订阅
     */
    @DeleteMapping("/users/{userId}/topic_subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable Long userId,
            @RequestParam Long streamId,
            @RequestParam String topic) {
        
        subscriptionService.unsubscribe(userId, streamId, topic);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取用户的所有订阅
     */
    @GetMapping("/users/{userId}/topic_subscriptions")
    public ResponseEntity<List<TopicSubscriptionDTO>> getUserSubscriptions(@PathVariable Long userId) {
        List<TopicSubscriptionDTO> subscriptions = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }
    
    /**
     * 获取用户在Stream的订阅
     */
    @GetMapping("/users/{userId}/streams/{streamId}/topic_subscriptions")
    public ResponseEntity<List<TopicSubscriptionDTO>> getUserStreamSubscriptions(
            @PathVariable Long userId,
            @PathVariable Long streamId) {
        
        List<TopicSubscriptionDTO> subscriptions = subscriptionService.getUserStreamSubscriptions(userId, streamId);
        return ResponseEntity.ok(subscriptions);
    }
    
    /**
     * 获取话题的订阅者
     */
    @GetMapping("/streams/{streamId}/topics/{topic}/subscribers")
    public ResponseEntity<List<TopicSubscriptionDTO>> getTopicSubscribers(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        
        List<TopicSubscriptionDTO> subscribers = subscriptionService.getTopicSubscribers(streamId, topic);
        return ResponseEntity.ok(subscribers);
    }
    
    /**
     * 设置静音状态
     */
    @PatchMapping("/users/{userId}/topic_subscriptions/mute")
    public ResponseEntity<Void> setMuted(
            @PathVariable Long userId,
            @RequestParam Long streamId,
            @RequestParam String topic,
            @RequestParam boolean muted) {
        
        subscriptionService.setMuted(userId, streamId, topic, muted);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 设置通知配置
     */
    @PatchMapping("/users/{userId}/topic_subscriptions/notifications")
    public ResponseEntity<Void> setNotificationSettings(
            @PathVariable Long userId,
            @RequestParam Long streamId,
            @RequestParam String topic,
            @RequestParam String settings) {
        
        subscriptionService.setNotificationSettings(userId, streamId, topic, settings);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 检查是否订阅
     */
    @GetMapping("/users/{userId}/topic_subscriptions/check")
    public ResponseEntity<Boolean> isSubscribed(
            @PathVariable Long userId,
            @RequestParam Long streamId,
            @RequestParam String topic) {
        
        boolean subscribed = subscriptionService.isSubscribed(userId, streamId, topic);
        return ResponseEntity.ok(subscribed);
    }
    
    /**
     * 获取订阅详情
     */
    @GetMapping("/users/{userId}/topic_subscriptions/detail")
    public ResponseEntity<TopicSubscriptionDTO> getSubscription(
            @PathVariable Long userId,
            @RequestParam Long streamId,
            @RequestParam String topic) {
        
        return subscriptionService.getSubscription(userId, streamId, topic)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 统计订阅者数量
     */
    @GetMapping("/streams/{streamId}/topics/{topic}/subscriber_count")
    public ResponseEntity<Long> getSubscriberCount(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        
        long count = subscriptionService.getSubscriberCount(streamId, topic);
        return ResponseEntity.ok(count);
    }
}

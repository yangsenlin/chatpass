package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.TopicDTO;
import com.chatpass.entity.TopicSubscription;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.TopicSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Topic Subscription 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Topic Subscriptions", description = "话题订阅 API")
public class TopicSubscriptionController {

    private final TopicSubscriptionService subscriptionService;
    private final SecurityUtil securityUtil;

    @PostMapping("/topic-subscriptions")
    @Operation(summary = "订阅话题")
    public ResponseEntity<ApiResponse<TopicDTO.SubscriptionResponse>> subscribe(
            @RequestBody TopicDTO.SubscribeRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        TopicSubscription sub = subscriptionService.subscribe(
                userId, request.getStreamId(), request.getTopicName(),
                request.getSubscriptionType(), request.getDesktopNotifications(),
                request.getEmailNotifications(), request.getPushNotifications());
        
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.toResponse(sub)));
    }

    @PostMapping("/topic-subscriptions/batch")
    @Operation(summary = "批量订阅")
    public ResponseEntity<ApiResponse<List<TopicDTO.SubscriptionResponse>>> batchSubscribe(
            @RequestBody TopicDTO.BatchSubscribeRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<TopicSubscription> subs = subscriptionService.batchSubscribe(userId, request.getSubscriptions());
        
        List<TopicDTO.SubscriptionResponse> response = subs.stream()
                .map(subscriptionService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/topic-subscriptions")
    @Operation(summary = "取消订阅")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @RequestParam Long streamId, @RequestParam String topicName) {
        Long userId = securityUtil.getCurrentUserId();
        
        subscriptionService.unsubscribe(userId, streamId, topicName);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/topic-subscriptions/mute")
    @Operation(summary = "静音话题")
    public ResponseEntity<ApiResponse<TopicDTO.SubscriptionResponse>> muteTopic(
            @RequestBody TopicDTO.MuteRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        TopicSubscription sub = subscriptionService.muteTopic(
                userId, request.getStreamId(), request.getTopicName());
        
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.toResponse(sub)));
    }

    @PostMapping("/topic-subscriptions/unmute")
    @Operation(summary = "取消静音")
    public ResponseEntity<ApiResponse<TopicDTO.SubscriptionResponse>> unmuteTopic(
            @RequestBody TopicDTO.MuteRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        TopicSubscription sub = subscriptionService.unmuteTopic(
                userId, request.getStreamId(), request.getTopicName());
        
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.toResponse(sub)));
    }

    @PostMapping("/topic-subscriptions/mention-only")
    @Operation(summary = "仅提及通知")
    public ResponseEntity<ApiResponse<TopicDTO.SubscriptionResponse>> setMentionOnly(
            @RequestBody TopicDTO.MuteRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        TopicSubscription sub = subscriptionService.setMentionOnly(
                userId, request.getStreamId(), request.getTopicName());
        
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.toResponse(sub)));
    }

    @GetMapping("/topic-subscriptions")
    @Operation(summary = "获取我的订阅列表")
    public ResponseEntity<ApiResponse<List<TopicDTO.SubscriptionResponse>>> getMySubscriptions() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<TopicSubscription> subs = subscriptionService.getUserSubscriptions(userId);
        
        List<TopicDTO.SubscriptionResponse> response = subs.stream()
                .map(subscriptionService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/topic-subscriptions/muted")
    @Operation(summary = "获取静音列表")
    public ResponseEntity<ApiResponse<List<TopicDTO.SubscriptionResponse>>> getMutedTopics() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<TopicSubscription> subs = subscriptionService.getMutedTopics(userId);
        
        List<TopicDTO.SubscriptionResponse> response = subs.stream()
                .map(subscriptionService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/topic-subscriptions/notified")
    @Operation(summary = "获取通知列表")
    public ResponseEntity<ApiResponse<List<TopicDTO.SubscriptionResponse>>> getNotifyTopics() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<TopicSubscription> subs = subscriptionService.getNotifyTopics(userId);
        
        List<TopicDTO.SubscriptionResponse> response = subs.stream()
                .map(subscriptionService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/topic-subscriptions/count")
    @Operation(summary = "统计订阅数")
    public ResponseEntity<ApiResponse<Long>> countSubscriptions() {
        Long userId = securityUtil.getCurrentUserId();
        
        Long count = subscriptionService.countSubscriptions(userId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/topic-subscriptions/stream/{streamId}")
    @Operation(summary = "获取 Stream 的订阅列表")
    public ResponseEntity<ApiResponse<List<TopicDTO.SubscriptionResponse>>> getStreamSubscriptions(
            @PathVariable Long streamId) {
        List<TopicSubscription> subs = subscriptionService.getStreamSubscriptions(streamId);
        
        List<TopicDTO.SubscriptionResponse> response = subs.stream()
                .map(subscriptionService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
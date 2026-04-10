package com.chatpass.service;

import com.chatpass.dto.TypingDTO;
import com.chatpass.entity.*;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Typing 服务
 * 
 * 输入状态管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TypingService {

    private final TypingEventRepository typingEventRepository;
    private final UserProfileRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 输入状态持续时间（秒）
    private static final int TYPING_DURATION_SECONDS = 15;

    /**
     * 开始输入
     */
    @Transactional
    public void startTyping(Long userId, Long realmId, TypingDTO.StartRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Recipient recipient = recipientRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // 清除该用户之前的输入状态
        typingEventRepository.deleteUserTypingEvents(userId, request.getRecipientId());

        // 创建新的输入事件
        TypingEvent typingEvent = TypingEvent.builder()
                .user(user)
                .recipient(recipient)
                .eventType("start")
                .topic(request.getTopic())
                .expiresAt(LocalDateTime.now().plusSeconds(TYPING_DURATION_SECONDS))
                .build();

        typingEventRepository.save(typingEvent);

        // 广播输入状态
        broadcastTypingStatus(user, recipient, request.getTopic(), "start");

        log.debug("User {} started typing in recipient {}", userId, request.getRecipientId());
    }

    /**
     * 停止输入
     */
    @Transactional
    public void stopTyping(Long userId, TypingDTO.StopRequest request) {
        // 清除输入状态
        typingEventRepository.deleteUserTypingEvents(userId, request.getRecipientId());

        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Recipient recipient = recipientRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // 广播停止输入
        broadcastTypingStatus(user, recipient, request.getTopic(), "stop");

        log.debug("User {} stopped typing in recipient {}", userId, request.getRecipientId());
    }

    /**
     * 获取当前正在输入的用户
     */
    @Transactional(readOnly = true)
    public TypingDTO.StatusResponse getTypingStatus(Long recipientId, String topic) {
        LocalDateTime now = LocalDateTime.now();

        List<TypingEvent> events;
        if (topic != null && !topic.isEmpty()) {
            events = typingEventRepository.findActiveTypingInTopic(recipientId, topic, now);
        } else {
            events = typingEventRepository.findActiveTypingInDirectMessage(recipientId, now);
        }

        List<TypingDTO.TypingUser> typingUsers = events.stream()
                .map(e -> TypingDTO.TypingUser.builder()
                        .id(e.getUser().getId())
                        .fullName(e.getUser().getFullName())
                        .email(e.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());

        return TypingDTO.StatusResponse.builder()
                .recipientId(recipientId)
                .topic(topic)
                .typingUsers(typingUsers)
                .build();
    }

    /**
     * 清理过期的输入事件
     */
    @Transactional
    public int cleanExpiredEvents() {
        int deleted = typingEventRepository.deleteExpiredEvents(LocalDateTime.now());
        if (deleted > 0) {
            log.debug("Cleaned {} expired typing events", deleted);
        }
        return deleted;
    }

    /**
     * 广播输入状态
     */
    private void broadcastTypingStatus(UserProfile user, Recipient recipient, String topic, String op) {
        TypingDTO.Event event = TypingDTO.Event.builder()
                .type("typing")
                .op(op)
                .senderId(user.getId())
                .senderName(user.getFullName())
                .recipientId(recipient.getId())
                .topic(topic)
                .build();

        if (recipient.getType() == Recipient.TYPE_STREAM) {
            // Stream 消息 - 广播给订阅者
            List<Long> subscriberIds = subscriptionRepository.findByStreamIdAndActiveTrue(recipient.getStreamId())
                    .stream()
                    .map(s -> s.getUserProfile().getId())
                    .collect(Collectors.toList());

            event.setUserIds(subscriberIds);

            messagingTemplate.convertAndSend(
                    "/topic/realm/" + user.getRealm().getId() + "/stream/" + recipient.getStreamId() + "/typing",
                    event);
        } else {
            // 私信 - 发送给参与者
            messagingTemplate.convertAndSend(
                    "/topic/realm/" + user.getRealm().getId() + "/private/typing",
                    event);
        }
    }
}
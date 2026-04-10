package com.chatpass.service;

import com.chatpass.dto.MessageDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import com.chatpass.websocket.WebSocketEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message 服务 - Zulip 消息管理
 * 
 * Zulip 消息核心概念：
 * - Stream Message: 发送到频道，有 topic
 * - Direct Message: 私信，没有 topic
 * 
 * 集成功能：
 * - Markdown 渲染
 * - @提及检测
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final StreamRepository streamRepository;
    private final RecipientRepository recipientRepository;
    private final RealmRepository realmRepository;
    private final UserMessageRepository userMessageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MarkdownService markdownService;
    private final ApplicationEventPublisher eventPublisher;

    // DM 的特殊 topic 标记
    private static final String DM_TOPIC = "\u0007";

    /**
     * 发送消息到 Stream（增强版）
     * 集成 Markdown 渲染和 @提及检测
     */
    @Transactional
    public MessageDTO.Response sendStreamMessage(Long realmId, Long senderId, 
            Long streamId, String topic, String content) {
        
        UserProfile sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));
        
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));
        
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // 获取或创建 Recipient
        Recipient recipient = recipientRepository.findByTypeAndStreamId(Recipient.TYPE_STREAM, streamId)
                .orElseGet(() -> recipientRepository.save(Recipient.builder()
                        .type(Recipient.TYPE_STREAM)
                        .streamId(streamId)
                        .build()));

        // 检测 @提及
        MarkdownService.MentionResult mentionResult = markdownService.detectMentions(content);
        
        // 渲染 Markdown
        String renderedContent = markdownService.render(content);

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .realm(realm)
                .subject(topic != null ? topic : "")
                .content(content)
                .renderedContent(renderedContent)
                .renderedContentVersion(MarkdownService.RENDER_VERSION)
                .dateSent(LocalDateTime.now())
                .isChannelMessage(true)
                .build();

        message = messageRepository.save(message);

        // 为订阅者创建 UserMessage
        createUserMessagesForStream(message, stream, mentionResult);

        // 发布 WebSocket 事件
        List<Long> subscriberIds = getStreamSubscribers(streamId);
        eventPublisher.publishEvent(new WebSocketEventHandler.MessageEvent(message, subscriberIds));

        return toResponse(message, stream.getName());
    }

    /**
     * 发送私信（增强版）
     */
    @Transactional
    public MessageDTO.Response sendDirectMessage(Long realmId, Long senderId,
            List<Long> recipientUserIds, String content) {
        
        UserProfile sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));
        
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // 创建 Private Recipient
        Recipient recipient = Recipient.builder()
                .type(Recipient.TYPE_PRIVATE)
                .build();
        recipient = recipientRepository.save(recipient);

        // 渲染 Markdown
        String renderedContent = markdownService.render(content);

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .realm(realm)
                .subject(DM_TOPIC)
                .content(content)
                .renderedContent(renderedContent)
                .renderedContentVersion(MarkdownService.RENDER_VERSION)
                .dateSent(LocalDateTime.now())
                .isChannelMessage(false)
                .build();

        message = messageRepository.save(message);

        // 为所有收件人创建 UserMessage
        List<Long> allUserIds = new ArrayList<>(recipientUserIds);
        allUserIds.add(senderId);
        createUserMessagesForPrivate(message, allUserIds, senderId);

        // 发布 WebSocket 事件
        eventPublisher.publishEvent(new WebSocketEventHandler.MessageEvent(message, allUserIds));

        return toResponse(message, null);
    }

    /**
     * 获取 Stream 中的消息
     */
    @Transactional(readOnly = true)
    public MessageDTO.ListResponse getStreamMessages(Long realmId, Long streamId, 
            String topic, int page, int pageSize) {
        
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));

        Recipient recipient = recipientRepository.findStreamRecipient(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient for stream", streamId));

        List<Message> messages;
        if (topic != null && !topic.isEmpty()) {
            messages = messageRepository.findByRecipientIdAndSubjectOrderByDateSentAsc(
                    recipient.getId(), topic);
        } else {
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Message> messagePage = messageRepository.findByRecipientIdOrderByDateSentDesc(
                    recipient.getId(), pageable);
            messages = messagePage.getContent();
        }

        List<MessageDTO.Response> responses = messages.stream()
                .map(m -> toResponse(m, stream.getName()))
                .collect(Collectors.toList());

        return MessageDTO.ListResponse.builder()
                .messages(responses)
                .anchor(messages.isEmpty() ? null : messages.get(messages.size() - 1).getId().toString())
                .historyLimited(false)
                .build();
    }

    /**
     * 获取消息详情
     */
    @Transactional(readOnly = true)
    public MessageDTO.Response getById(Long realmId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        if (!message.getRealm().getId().equals(realmId)) {
            throw new ResourceNotFoundException("Message", messageId);
        }

        String streamName = null;
        if (message.getIsChannelMessage() && message.getRecipient().getStreamId() != null) {
            streamRepository.findById(message.getRecipient().getStreamId())
                    .ifPresent(s -> streamName = s.getName());
        }

        return toResponse(message, streamName);
    }

    /**
     * 编辑消息（增强版）
     * 支持编辑历史记录
     */
    @Transactional
    public MessageDTO.Response update(Long realmId, Long messageId, Long userId,
            String newTopic, String newContent) {
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        if (!message.getRealm().getId().equals(realmId)) {
            throw new ResourceNotFoundException("Message", messageId);
        }

        if (!message.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the sender can edit the message");
        }

        String oldTopic = message.getSubject();
        
        if (newTopic != null && message.getIsChannelMessage()) {
            message.setSubject(newTopic);
        }
        
        if (newContent != null) {
            // 记录编辑历史
            String editHistory = message.getEditHistory();
            String newEntry = String.format(
                    "{\"prev_subject\":\"%s\",\"timestamp\":%d,\"user_id\":%d}",
                    escapeJson(oldTopic),
                    System.currentTimeMillis(),
                    userId
            );
            
            if (editHistory == null || editHistory.isEmpty()) {
                message.setEditHistory("[" + newEntry + "]");
            } else {
                message.setEditHistory(editHistory.replace("]", "," + newEntry + "]"));
            }
            
            // 更新内容并重新渲染
            message.setContent(newContent);
            message.setRenderedContent(markdownService.render(newContent));
            message.setRenderedContentVersion(MarkdownService.RENDER_VERSION);
        }
        
        message.setLastEditTime(LocalDateTime.now());
        message = messageRepository.save(message);

        // 发布更新事件
        Long streamId = message.getRecipient().getStreamId();
        eventPublisher.publishEvent(new WebSocketEventHandler.MessageUpdateEvent(
                messageId, realmId, streamId, userId, 
                message.getRenderedContent(), newTopic, oldTopic));

        String streamName = null;
        if (message.getIsChannelMessage() && streamId != null) {
            streamRepository.findById(streamId)
                    .ifPresent(s -> streamName = s.getName());
        }

        return toResponse(message, streamName);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private MessageDTO.Response toResponse(Message message, String streamName) {
        UserProfile sender = message.getSender();
        
        return MessageDTO.Response.builder()
                .id(message.getId())
                .type(message.getIsChannelMessage() ? "stream" : "private")
                .subject(message.getIsChannelMessage() ? message.getSubject() : null)
                .content(message.getContent())
                .renderedContent(message.getRenderedContent())
                .senderId(sender.getId())
                .senderFullName(sender.getFullName())
                .senderEmail(sender.getEmail())
                .senderAvatarUrl(sender.getAvatarUrl())
                .recipientId(message.getRecipient().getId())
                .streamId(message.getRecipient().getStreamId())
                .streamName(streamName)
                .dateSent(message.getDateSent())
                .lastEditTime(message.getLastEditTime())
                .hasAttachment(message.getHasAttachment())
                .hasImage(message.getHasImage())
                .hasLink(message.getHasLink())
                .isChannelMessage(message.getIsChannelMessage())
                .build();
    }

    // ==================== 辅助方法 ====================

    /**
     * 为 Stream 消息创建 UserMessage
     */
    private void createUserMessagesForStream(Message message, Stream stream, 
            MarkdownService.MentionResult mentionResult) {
        List<Subscription> subscriptions = subscriptionRepository.findByStreamId(stream.getId());
        
        for (Subscription sub : subscriptions) {
            if (!sub.getActive()) continue;
            
            UserProfile user = sub.getUserProfile();
            long flags = 0L;
            
            // 检测 @提及
            if (mentionResult.mentionedUsers.contains(user.getEmail()) ||
                mentionResult.mentionedUsers.contains(user.getFullName())) {
                flags |= UserMessage.FLAG_MENTIONED;
            }
            
            // 检测 @all/@everyone
            if (mentionResult.hasWildcardMention) {
                flags |= UserMessage.FLAG_WILDCARD_MENTIONED;
            }
            
            // 发送者标记已读
            if (user.getId().equals(message.getSender().getId())) {
                flags |= UserMessage.FLAG_READ;
            }
            
            UserMessage um = UserMessage.builder()
                    .userProfile(user)
                    .message(message)
                    .flags(flags)
                    .build();
            
            userMessageRepository.save(um);
        }
    }

    /**
     * 为私信创建 UserMessage
     */
    private void createUserMessagesForPrivate(Message message, List<Long> userIds, Long senderId) {
        for (Long userId : userIds) {
            UserProfile user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            
            long flags = 0L;
            
            // 发送者自动标记已读
            if (userId.equals(senderId)) {
                flags |= UserMessage.FLAG_READ;
            }
            
            UserMessage um = UserMessage.builder()
                    .userProfile(user)
                    .message(message)
                    .flags(flags)
                    .build();
            
            userMessageRepository.save(um);
        }
    }

    /**
     * 获取 Stream 的订阅者列表
     */
    private List<Long> getStreamSubscribers(Long streamId) {
        return subscriptionRepository.findByStreamId(streamId)
                .stream()
                .filter(Subscription::getActive)
                .map(s -> s.getUserProfile().getId())
                .collect(Collectors.toList());
    }
}
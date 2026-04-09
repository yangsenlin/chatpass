package com.chatpass.service;

import com.chatpass.dto.MessageDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message 服务 - Zulip 消息管理
 * 
 * Zulip 消息核心概念：
 * - Stream Message: 发送到频道，有 topic
 * - Direct Message: 私信，没有 topic
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final StreamRepository streamRepository;
    private final RecipientRepository recipientRepository;
    private final RealmRepository realmRepository;
    private final ClientRepository clientRepository;

    // DM 的特殊 topic 标记
    private static final String DM_TOPIC = "\u0007";

    /**
     * 发送消息到 Stream
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
        Recipient recipient = recipientRepository.findStreamRecipient(streamId)
                .orElseGet(() -> recipientRepository.save(Recipient.builder()
                        .type(Recipient.TYPE_STREAM)
                        .streamId(streamId)
                        .build()));

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .realm(realm)
                .subject(topic != null ? topic : "")
                .content(content)
                .dateSent(LocalDateTime.now())
                .isChannelMessage(true)
                .build();

        message = messageRepository.save(message);

        return toResponse(message, stream.getName());
    }

    /**
     * 发送私信
     */
    @Transactional
    public MessageDTO.Response sendDirectMessage(Long realmId, Long senderId,
            List<Long> recipientUserIds, String content) {
        
        UserProfile sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));
        
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // TODO: 创建 Private Recipient (需要处理多个收件人)

        Recipient recipient = Recipient.builder()
                .type(Recipient.TYPE_PRIVATE)
                .build();
        recipient = recipientRepository.save(recipient);

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .realm(realm)
                .subject(DM_TOPIC)
                .content(content)
                .dateSent(LocalDateTime.now())
                .isChannelMessage(false)
                .build();

        message = messageRepository.save(message);

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
     * 编辑消息
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

        if (newTopic != null && message.getIsChannelMessage()) {
            message.setSubject(newTopic);
        }
        if (newContent != null) {
            message.setContent(newContent);
        }
        message.setLastEditTime(LocalDateTime.now());

        message = messageRepository.save(message);

        String streamName = null;
        if (message.getIsChannelMessage() && message.getRecipient().getStreamId() != null) {
            streamRepository.findById(message.getRecipient().getStreamId())
                    .ifPresent(s -> streamName = s.getName());
        }

        return toResponse(message, streamName);
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
}
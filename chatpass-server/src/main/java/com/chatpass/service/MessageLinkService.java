package com.chatpass.service;

import com.chatpass.dto.MessageDTO;
import com.chatpass.dto.MessageLinkDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MessageLink 服务
 * 
 * 消息引用、转发管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageLinkService {

    private final MessageLinkRepository messageLinkRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final StreamRepository streamRepository;
    private final MarkdownService markdownService;
    private final MessageService messageService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建引用链接
     */
    @Transactional
    public MessageLinkDTO.Response createLink(Long userId, MessageLinkDTO.CreateRequest request) {
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message", request.getMessageId()));

        Message targetMessage = messageRepository.findById(request.getTargetMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Target Message", request.getTargetMessageId()));

        // 检查是否已存在
        if (messageLinkRepository.existsByMessageIdAndTargetMessageId(request.getMessageId(), request.getTargetMessageId())) {
            throw new IllegalArgumentException("Link already exists");
        }

        MessageLink link = MessageLink.builder()
                .message(message)
                .targetMessage(targetMessage)
                .linkType(request.getLinkType() != null ? request.getLinkType() : MessageLink.LINK_TYPE_REFERENCE)
                .build();

        link = messageLinkRepository.save(link);

        log.info("User {} created {} link from message {} to {}", 
                userId, link.getLinkType(), request.getMessageId(), request.getTargetMessageId());

        return toResponse(link);
    }

    /**
     * 转发消息
     */
    @Transactional
    public MessageDTO.Response forwardMessage(Long userId, Long realmId, MessageLinkDTO.ForwardRequest request) {
        UserProfile sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Message sourceMessage = messageRepository.findById(request.getSourceMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Source Message", request.getSourceMessageId()));

        // 构建转发内容
        String forwardedContent = buildForwardedContent(sourceMessage);

        // 发送新消息
        MessageDTO.Response newMessageResponse;
        if ("stream".equals(request.getType())) {
            newMessageResponse = messageService.sendStreamMessage(
                    realmId, userId, request.getRecipientId(), request.getTopic(), forwardedContent);
        } else {
            newMessageResponse = messageService.sendDirectMessage(
                    realmId, userId, java.util.List.of(request.getRecipientId()), forwardedContent);
        }

        // 创建转发链接
        Long newMessageId = newMessageResponse.getId();
        Message newMsg = messageRepository.findById(newMessageId).orElse(null);
        if (newMsg != null) {
            MessageLink link = MessageLink.builder()
                    .message(newMsg)
                    .targetMessage(sourceMessage)
                    .linkType(MessageLink.LINK_TYPE_FORWARD)
                    .build();
            messageLinkRepository.save(link);
        }

        log.info("User {} forwarded message {} to recipient {}", userId, request.getSourceMessageId(), request.getRecipientId());

        return newMessageResponse;
    }

    /**
     * 获取消息的引用关系
     */
    @Transactional(readOnly = true)
    public MessageLinkDTO.ListResponse getMessageLinks(Long messageId) {
        // 该消息引用的其他消息
        List<MessageLink> references = messageLinkRepository.findByMessageId(messageId);

        // 引用该消息的其他消息
        List<MessageLink> referencedBy = messageLinkRepository.findReferencesToMessage(messageId);

        return MessageLinkDTO.ListResponse.builder()
                .messageId(messageId)
                .references(references.stream().map(this::toResponse).collect(Collectors.toList()))
                .referencedBy(referencedBy.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    /**
     * 删除引用链接
     */
    @Transactional
    public void deleteLink(Long linkId, Long userId) {
        MessageLink link = messageLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("MessageLink", linkId));

        messageLinkRepository.delete(link);

        log.info("User {} deleted message link {}", userId, linkId);
    }

    /**
     * 构建转发内容
     */
    private String buildForwardedContent(Message sourceMessage) {
        StringBuilder sb = new StringBuilder();

        sb.append("_Forwarded message:_\n\n");
        sb.append("**From:** ").append(sourceMessage.getSender().getFullName()).append("\n");

        if (sourceMessage.getIsChannelMessage()) {
            String streamName = "unknown";
            if (sourceMessage.getRecipient().getStreamId() != null) {
                java.util.Optional<Stream> streamOpt = streamRepository.findById(sourceMessage.getRecipient().getStreamId());
                if (streamOpt.isPresent()) {
                    streamName = streamOpt.get().getName();
                }
            }
            sb.append("**In:** #").append(streamName);
            if (sourceMessage.getSubject() != null && !sourceMessage.getSubject().isEmpty()) {
                sb.append(" > ").append(sourceMessage.getSubject());
            }
            sb.append("\n");
        }

        sb.append("**Date:** ").append(sourceMessage.getDateSent()).append("\n\n");
        sb.append("---\n\n");
        sb.append(sourceMessage.getContent());

        return sb.toString();
    }

    private MessageLinkDTO.Response toResponse(MessageLink link) {
        return MessageLinkDTO.Response.builder()
                .id(link.getId())
                .messageId(link.getMessage().getId())
                .targetMessageId(link.getTargetMessage().getId())
                .linkType(link.getLinkType())
                .dateCreated(link.getDateCreated())
                .build();
    }
}
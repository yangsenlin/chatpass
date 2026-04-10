package com.chatpass.service;

import com.chatpass.dto.MessageDTO;
import com.chatpass.dto.NarrowDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Narrow 查询服务
 * 
 * Zulip 的核心消息过滤机制
 * 支持多种过滤条件组合查询
 */
@Service
@RequiredArgsConstructor
public class NarrowService {

    private final MessageRepository messageRepository;
    private final StreamRepository streamRepository;
    private final RecipientRepository recipientRepository;
    private final UserProfileRepository userRepository;
    private final UserMessageRepository userMessageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RealmRepository realmRepository;

    /**
     * 执行 Narrow 查询
     */
    @Transactional(readOnly = true)
    public NarrowDTO.Response query(Long realmId, Long userId, NarrowDTO.Request request) {
        List<NarrowDTO.Filter> filters = request.getNarrow();
        if (filters == null || filters.isEmpty()) {
            return queryHomeView(realmId, userId, request);
        }

        QueryContext ctx = new QueryContext(realmId, userId, filters);
        List<Message> messages = executeQuery(ctx, request);
        
        List<MessageDTO.Response> responses = messages.stream()
                .map(m -> toResponse(m, ctx))
                .collect(Collectors.toList());

        String anchor = responses.isEmpty() ? null : 
                (request.getAnchor() != null ? request.getAnchor().toString() : responses.get(0).getId().toString());

        return NarrowDTO.Response.builder()
                .messages(responses)
                .anchor(anchor)
                .anchoredToNewest(messages.isEmpty())
                .anchoredToOldest(messages.isEmpty())
                .historyLimited(false)
                .build();
    }

    /**
     * Home View 查询
     */
    private NarrowDTO.Response queryHomeView(Long realmId, Long userId, NarrowDTO.Request request) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserProfileIdAndActiveTrue(userId);
        List<Long> streamIds = subscriptions.stream()
                .map(s -> s.getStream().getId())
                .collect(Collectors.toList());

        List<Recipient> recipients = recipientRepository.findByStreamIdIn(streamIds);
        List<Long> recipientIds = recipients.stream().map(Recipient::getId).collect(Collectors.toList());

        Pageable pageable = PageRequest.of(0, 
                (request.getNumBefore() != null ? request.getNumBefore() : 50) + 
                (request.getNumAfter() != null ? request.getNumAfter() : 50));

        Page<Message> messagePage = messageRepository.findByRecipientIdInOrderByDateSentDesc(recipientIds, pageable);

        List<MessageDTO.Response> responses = messagePage.getContent().stream()
                .map(this::toResponseWithStreamName)
                .collect(Collectors.toList());

        return NarrowDTO.Response.builder()
                .messages(responses)
                .anchor(messagePage.isEmpty() ? null : messagePage.getContent().get(0).getId().toString())
                .anchoredToNewest(messagePage.isLast())
                .anchoredToOldest(messagePage.isFirst())
                .historyLimited(false)
                .build();
    }

    /**
     * 执行具体查询
     */
    private List<Message> executeQuery(QueryContext ctx, NarrowDTO.Request request) {
        List<Long> recipientIds = resolveRecipients(ctx);
        List<String> subjects = resolveSubjects(ctx);
        Long senderId = resolveSender(ctx);
        String searchQuery = resolveSearch(ctx);
        Boolean unreadOnly = resolveUnread(ctx, ctx.userId);

        if (!subjects.isEmpty() && recipientIds.size() == 1) {
            Long recipientId = recipientIds.get(0);
            String topic = subjects.get(0);
            return messageRepository.findByRecipientIdAndSubjectOrderByDateSentAsc(recipientId, topic);
        }

        if (!recipientIds.isEmpty()) {
            Pageable pageable = buildPageable(request);
            return messageRepository.findByRecipientIdInOrderByDateSentDesc(recipientIds, pageable).getContent();
        }

        if (senderId != null) {
            return messageRepository.findBySenderIdOrderByDateSentDesc(senderId);
        }

        if (searchQuery != null) {
            return messageRepository.searchByContent(ctx.realmId, searchQuery);
        }

        Pageable pageable = buildPageable(request);
        return messageRepository.findByRealmIdOrderByDateSentDesc(ctx.realmId, pageable).getContent();
    }

    private List<Long> resolveRecipients(QueryContext ctx) {
        List<Long> recipientIds = new ArrayList<>();
        for (NarrowDTO.Filter filter : ctx.filters) {
            String op = filter.getOperator();
            if (NarrowDTO.Operators.STREAM.equals(op)) {
                Stream stream = streamRepository.findByRealmIdAndName(ctx.realmId, filter.getOperand())
                        .orElseThrow(() -> new ResourceNotFoundException("Stream not found: " + filter.getOperand()));
                Recipient recipient = recipientRepository.findByTypeAndStreamId(Recipient.TYPE_STREAM, stream.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Recipient for stream", stream.getId()));
                recipientIds.add(recipient.getId());
            }
            if (NarrowDTO.Operators.IN.equals(op) && NarrowDTO.Operands.PRIVATE.equals(filter.getOperand())) {
                List<Recipient> privateRecipients = recipientRepository.findByType(Recipient.TYPE_PRIVATE);
                recipientIds.addAll(privateRecipients.stream().map(Recipient::getId).collect(Collectors.toList()));
            }
        }
        return recipientIds;
    }

    private List<String> resolveSubjects(QueryContext ctx) {
        return ctx.filters.stream()
                .filter(f -> NarrowDTO.Operators.TOPIC.equals(f.getOperator()))
                .map(NarrowDTO.Filter::getOperand)
                .collect(Collectors.toList());
    }

    private Long resolveSender(QueryContext ctx) {
        for (NarrowDTO.Filter filter : ctx.filters) {
            if (NarrowDTO.Operators.SENDER.equals(filter.getOperator())) {
                return userRepository.findByEmail(filter.getOperand())
                        .map(UserProfile::getId)
                        .orElseGet(() -> {
                            try { return Long.parseLong(filter.getOperand()); }
                            catch (NumberFormatException e) { return null; }
                        });
            }
        }
        return null;
    }

    private String resolveSearch(QueryContext ctx) {
        return ctx.filters.stream()
                .filter(f -> NarrowDTO.Operators.SEARCH.equals(f.getOperator()))
                .map(NarrowDTO.Filter::getOperand)
                .findFirst()
                .orElse(null);
    }

    private Boolean resolveUnread(QueryContext ctx, Long userId) {
        return ctx.filters.stream()
                .filter(f -> NarrowDTO.Operators.UNREAD.equals(f.getOperator()) || 
                             (NarrowDTO.Operators.IS.equals(f.getOperator()) && 
                              NarrowDTO.Operands.UNREAD.equals(f.getOperand())))
                .findFirst()
                .isPresent();
    }

    private Pageable buildPageable(NarrowDTO.Request request) {
        return PageRequest.of(0, 
                (request.getNumBefore() != null ? request.getNumBefore() : 50) + 
                (request.getNumAfter() != null ? request.getNumAfter() : 50) + 1);
    }

    private MessageDTO.Response toResponse(Message message, QueryContext ctx) {
        return toResponseWithStreamName(message);
    }

    private MessageDTO.Response toResponseWithStreamName(Message message) {
        UserProfile sender = message.getSender();
        String streamName = null;

        if (message.getRecipient().getStreamId() != null) {
            Optional<Stream> streamOpt = streamRepository.findById(message.getRecipient().getStreamId());
            if (streamOpt.isPresent()) {
                streamName = streamOpt.get().getName();
            }
        }

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

    private static class QueryContext {
        final Long realmId;
        final Long userId;
        final List<NarrowDTO.Filter> filters;

        QueryContext(Long realmId, Long userId, List<NarrowDTO.Filter> filters) {
            this.realmId = realmId;
            this.userId = userId;
            this.filters = filters;
        }
    }
}
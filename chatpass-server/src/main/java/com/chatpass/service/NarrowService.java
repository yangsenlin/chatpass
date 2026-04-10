package com.chatpass.service;

import com.chatpass.dto.MessageDTO;
import com.chatpass.dto.NarrowDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            // 无过滤条件，返回全部消息（用户订阅的消息）
            return queryHomeView(realmId, userId, request);
        }

        // 解析过滤条件
        QueryContext ctx = new QueryContext(realmId, userId, filters);

        // 根据过滤条件构建查询
        List<Message> messages = executeQuery(ctx, request);

        // 转换为响应
        List<MessageDTO.Response> responses = messages.stream()
                .map(m -> toResponse(m, ctx))
                .collect(Collectors.toList());

        // 构建锚点信息
        String anchor = null;
        if (!messages.isEmpty()) {
            if (request.getAnchor() != null) {
                anchor = request.getAnchor().toString();
            } else {
                anchor = messages.get(0).getId().toString();
            }
        }

        return NarrowDTO.Response.builder()
                .messages(responses)
                .anchor(anchor)
                .anchoredToNewest(messages.isEmpty() || messages.get(messages.size() - 1).getId().equals(findNewestId(ctx)))
                .anchoredToOldest(messages.isEmpty() || messages.get(0).getId().equals(findOldestId(ctx)))
                .historyLimited(false)
                .build();
    }

    /**
     * Home View 查询（用户订阅的所有 Stream）
     */
    private NarrowDTO.Response queryHomeView(Long realmId, Long userId, NarrowDTO.Request request) {
        // 获取用户订阅的 Stream
        List<Subscription> subscriptions = subscriptionRepository.findByUserProfileIdAndActiveTrue(userId);
        List<Long> streamIds = subscriptions.stream()
                .map(s -> s.getStream().getId())
                .collect(Collectors.toList());

        // 获取这些 Stream 的 Recipient
        List<Recipient> recipients = recipientRepository.findByStreamIdIn(streamIds);
        List<Long> recipientIds = recipients.stream()
                .map(Recipient::getId)
                .collect(Collectors.toList());

        // 分页查询
        Pageable pageable = PageRequest.of(
                request.getNumBefore() != null ? 0 : 0,
                request.getNumBefore() != null ? request.getNumBefore() + request.getNumAfter() : 100);

        Page<Message> messagePage = messageRepository.findByRecipientIdInOrderByDateSentDesc(recipientIds, pageable);

        List<MessageDTO.Response> responses = messagePage.getContent().stream()
                .map(m -> toResponseWithStreamName(m))
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
        // 构建查询条件
        List<Long> recipientIds = resolveRecipients(ctx);
        List<String> subjects = resolveSubjects(ctx);
        Long senderId = resolveSender(ctx);
        String searchQuery = resolveSearch(ctx);

        // 执行查询
        if (recipientIds.isEmpty() && senderId == null && searchQuery == null) {
            // 全部消息
            Pageable pageable = buildPageable(request);
            return messageRepository.findByRealmIdOrderByDateSentDesc(ctx.realmId, pageable).getContent();
        }

        if (!subjects.isEmpty() && recipientIds.size() == 1) {
            // Stream + Topic 查询
            Long recipientId = recipientIds.get(0);
            String topic = subjects.get(0);
            return messageRepository.findByRecipientIdAndSubjectOrderByDateSentAsc(recipientId, topic);
        }

        if (!recipientIds.isEmpty()) {
            // Stream 查询
            Pageable pageable = buildPageable(request);
            return messageRepository.findByRecipientIdInOrderByDateSentDesc(recipientIds, pageable).getContent();
        }

        if (senderId != null) {
            // 发送者查询
            return messageRepository.findBySenderIdOrderByDateSentDesc(senderId);
        }

        if (searchQuery != null) {
            // 搜索查询
            return messageRepository.searchByContent(ctx.realmId, searchQuery);
        }

        return Collections.emptyList();
    }

    /**
     * 解析 Recipient（Stream 或 Private）
     */
    private List<Long> resolveRecipients(QueryContext ctx) {
        List<Long> recipientIds = new ArrayList<>();

        for (NarrowDTO.Filter filter : ctx.filters) {
            String op = filter.getOperator();

            if (NarrowDTO.Operators.STREAM.equals(op)) {
                // Stream 过滤
                Stream stream = streamRepository.findByRealmIdAndName(ctx.realmId, filter.getOperand())
                        .orElseThrow(() -> new ResourceNotFoundException("Stream", filter.getOperand()));
                
                Recipient recipient = recipientRepository.findStreamRecipient(stream.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Recipient for stream", stream.getId()));
                
                recipientIds.add(recipient.getId());
            }

            if (NarrowDTO.Operators.IN.equals(op) && NarrowDTO.Operands.PRIVATE.equals(filter.getOperand())) {
                // 私信过滤
                List<Recipient> privateRecipients = recipientRepository.findByType(Recipient.TYPE_PRIVATE);
                recipientIds.addAll(privateRecipients.stream().map(Recipient::getId).collect(Collectors.toList()));
            }
        }

        return recipientIds;
    }

    /**
     * 解析 Topic
     */
    private List<String> resolveSubjects(QueryContext ctx) {
        List<String> subjects = new ArrayList<>();

        for (NarrowDTO.Filter filter : ctx.filters) {
            if (NarrowDTO.Operators.TOPIC.equals(filter.getOperator())) {
                subjects.add(filter.getOperand());
            }
        }

        return subjects;
    }

    /**
     * 解析发送者
     */
    private Long resolveSender(QueryContext ctx) {
        for (NarrowDTO.Filter filter : ctx.filters) {
            if (NarrowDTO.Operators.SENDER.equals(filter.getOperator())) {
                UserProfile user = userRepository.findByEmail(filter.getOperand())
                        .orElse(null);
                if (user != null) {
                    return user.getId();
                }
                // 尝试按 ID 查询
                try {
                    Long userId = Long.parseLong(filter.getOperand());
                    return userId;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 解析搜索关键词
     */
    private String resolveSearch(QueryContext ctx) {
        for (NarrowDTO.Filter filter : ctx.filters) {
            if (NarrowDTO.Operators.SEARCH.equals(filter.getOperator())) {
                return filter.getOperand();
            }
        }
        return null;
    }

    /**
     * 构建分页参数
     */
    private Pageable buildPageable(NarrowDTO.Request request) {
        int numBefore = request.getNumBefore() != null ? request.getNumBefore() : 50;
        int numAfter = request.getNumAfter() != null ? request.getNumAfter() : 50;
        return PageRequest.of(0, numBefore + numAfter + 1);
    }

    /**
     * 查找最新消息 ID
     */
    private Long findNewestId(QueryContext ctx) {
        // TODO: 实现高效的最新消息 ID 查询
        return null;
    }

    /**
     * 查找最旧消息 ID
     */
    private Long findOldestId(QueryContext ctx) {
        // TODO: 实现高效的最旧消息 ID 查询
        return null;
    }

    /**
     * 转换为响应
     */
    private MessageDTO.Response toResponse(Message message, QueryContext ctx) {
        String streamName = null;
        if (message.getRecipient().getStreamId() != null) {
            streamRepository.findById(message.getRecipient().getStreamId())
                    .ifPresent(s -> streamName = s.getName());
        }

        return toResponseWithStreamName(message);
    }

    private MessageDTO.Response toResponseWithStreamName(Message message) {
        UserProfile sender = message.getSender();
        String streamName = null;

        if (message.getRecipient().getStreamId() != null) {
            streamRepository.findById(message.getRecipient().getStreamId())
                    .ifPresent(s -> streamName = s.getName());
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

    /**
     * 查询上下文
     */
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
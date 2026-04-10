package com.chatpass.service;

import com.chatpass.dto.UserMessageDTO;
import com.chatpass.dto.WebSocketDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * UserMessage 服务
 * 
 * 管理用户对消息的状态（已读、标记、提及等）
 * 基于 Zulip 的 Flags 位掩码机制
 */
@Service
@RequiredArgsConstructor
public class UserMessageService {

    private final UserMessageRepository userMessageRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 更新消息 Flags
     */
    @Transactional
    public UserMessageDTO.FlagsResponse updateFlags(Long userId, UserMessageDTO.FlagsRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<Long> messageIds = request.getMessages();
        String flag = request.getFlag();
        boolean add = "add".equals(request.getOperation());

        List<UserMessage> userMessages = new ArrayList<>();

        for (Long messageId : messageIds) {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

            // 查找或创建 UserMessage
            UserMessage um = userMessageRepository.findByUserProfileIdAndMessageId(userId, messageId)
                    .orElseGet(() -> UserMessage.builder()
                            .userProfile(user)
                            .message(message)
                            .flags(0L)
                            .build());

            // 更新 Flag
            updateFlag(um, flag, add);

            userMessages.add(userMessageRepository.save(um));
        }

        return UserMessageDTO.FlagsResponse.builder()
                .messages(messageIds)
                .flag(flag)
                .added(add)
                .build();
    }

    /**
     * 标记全部已读
     */
    @Transactional
    public UserMessageDTO.FlagsResponse markAllAsRead(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 获取用户订阅的 Stream
        List<Subscription> subscriptions = subscriptionRepository.findByUserProfileIdAndActiveTrue(userId);
        List<Long> streamIds = subscriptions.stream()
                .map(s -> s.getStream().getId())
                .collect(Collectors.toList());

        // 获取这些 Stream 的消息（未读）
        List<UserMessage> unreadMessages = userMessageRepository.findByUserProfileIdAndFlagsNot(userId, UserMessage.FLAG_READ);

        // 标记已读
        for (UserMessage um : unreadMessages) {
            um.setRead(true);
            userMessageRepository.save(um);
        }

        List<Long> markedIds = unreadMessages.stream()
                .map(um -> um.getMessage().getId())
                .collect(Collectors.toList());

        return UserMessageDTO.FlagsResponse.builder()
                .messages(markedIds)
                .flag(UserMessageDTO.FlagTypes.READ)
                .added(true)
                .build();
    }

    /**
     * 获取未读消息摘要
     */
    @Transactional(readOnly = true)
    public UserMessageDTO.UnreadSummary getUnreadSummary(Long userId) {
        // 获取所有未读 UserMessage
        List<UserMessage> unread = userMessageRepository.findByUserProfileIdAndFlagsNot(userId, UserMessage.FLAG_READ);

        // 按 Stream/Topic 分组
        Map<Long, Map<String, List<Long>>> streamTopicMessages = new HashMap<>();
        Map<Long, List<Long>> privateMessages = new HashMap<>();

        for (UserMessage um : unread) {
            Message msg = um.getMessage();
            Recipient recipient = msg.getRecipient();

            if (recipient.getType() == Recipient.TYPE_STREAM) {
                Long streamId = recipient.getStreamId();
                String topic = msg.getSubject() != null ? msg.getSubject() : "";

                streamTopicMessages.computeIfAbsent(streamId, k -> new HashMap<>())
                        .computeIfAbsent(topic, k -> new ArrayList<>())
                        .add(msg.getId());
            } else {
                // 私信
                Long senderId = msg.getSender().getId();
                privateMessages.computeIfAbsent(senderId, k -> new ArrayList<>())
                        .add(msg.getId());
            }
        }

        // 构建 Stream 未读统计
        List<UserMessageDTO.UnreadStream> unreadStreams = new ArrayList<>();
        for (Map.Entry<Long, Map<String, List<Long>>> entry : streamTopicMessages.entrySet()) {
            Long streamId = entry.getKey();
            Map<String, List<Long>> topics = entry.getValue();

            List<UserMessageDTO.UnreadTopic> topicList = topics.entrySet().stream()
                    .map(t -> UserMessageDTO.UnreadTopic.builder()
                            .topic(t.getKey())
                            .unreadCount(t.getValue().size())
                            .messageIds(t.getValue())
                            .build())
                    .collect(Collectors.toList());

            int totalUnread = topics.values().stream()
                    .mapToInt(List::size)
                    .sum();

            unreadStreams.add(UserMessageDTO.UnreadStream.builder()
                    .streamId(streamId)
                    .unreadCount(totalUnread)
                    .topics(topicList)
                    .build());
        }

        // 构建私信未读统计
        List<UserMessageDTO.UnreadPm> unreadPms = privateMessages.entrySet().stream()
                .map(e -> UserMessageDTO.UnreadPm.builder()
                        .senderId(e.getKey())
                        .unreadCount(e.getValue().size())
                        .messageIds(e.getValue())
                        .build())
                .collect(Collectors.toList());

        // 提取提及的消息
        List<Long> mentionedMessages = unread.stream()
                .filter(um -> (um.getFlags() & UserMessage.FLAG_MENTIONED) != 0)
                .map(um -> um.getMessage().getId())
                .collect(Collectors.toList());

        long totalUnread = unread.size();

        return UserMessageDTO.UnreadSummary.builder()
                .unreadCount(totalUnread)
                .unreadStreams(unreadStreams)
                .unreadPms(unreadPms)
                .mentionedMessages(mentionedMessages)
                .build();
    }

    /**
     * 获取用户对消息的 Flags
     */
    @Transactional(readOnly = true)
    public List<String> getMessageFlags(Long userId, Long messageId) {
        UserMessage um = userMessageRepository.findByUserProfileIdAndMessageId(userId, messageId)
                .orElse(null);

        if (um == null) {
            return Collections.emptyList();
        }

        List<String> flags = new ArrayList<>();
        long flagBits = um.getFlags();

        if ((flagBits & UserMessage.FLAG_READ) != 0) flags.add("read");
        if ((flagBits & UserMessage.FLAG_STARRED) != 0) flags.add("starred");
        if ((flagBits & UserMessage.FLAG_COLLAPSED) != 0) flags.add("collapsed");
        if ((flagBits & UserMessage.FLAG_MENTIONED) != 0) flags.add("mentioned");
        if ((flagBits & UserMessage.FLAG_WILDCARD_MENTIONED) != 0) flags.add("wildcard_mentioned");
        if ((flagBits & UserMessage.FLAG_HAS_ALERT_WORD) != 0) flags.add("has_alert_word");
        if ((flagBits & UserMessage.FLAG_HISTORICAL) != 0) flags.add("historical");

        return flags;
    }

    /**
     * 更新单个 Flag
     */
    private void updateFlag(UserMessage um, String flag, boolean add) {
        long flagBit = getFlagBit(flag);
        if (flagBit == 0) return;

        if (add) {
            um.setFlags(um.getFlags() | flagBit);
        } else {
            um.setFlags(um.getFlags() & ~flagBit);
        }
    }

    /**
     * 获取 Flag 位掩码
     */
    private long getFlagBit(String flag) {
        switch (flag) {
            case UserMessageDTO.FlagTypes.READ:
                return UserMessage.FLAG_READ;
            case UserMessageDTO.FlagTypes.STARRED:
                return UserMessage.FLAG_STARRED;
            case UserMessageDTO.FlagTypes.COLLAPSED:
                return UserMessage.FLAG_COLLAPSED;
            case UserMessageDTO.FlagTypes.MENTIONED:
                return UserMessage.FLAG_MENTIONED;
            case UserMessageDTO.FlagTypes.WILDCARD_MENTIONED:
                return UserMessage.FLAG_WILDCARD_MENTIONED;
            case UserMessageDTO.FlagTypes.HAS_ALERT_WORD:
                return UserMessage.FLAG_HAS_ALERT_WORD;
            case UserMessageDTO.FlagTypes.HISTORICAL:
                return UserMessage.FLAG_HISTORICAL;
            default:
                return 0;
        }
    }

    /**
     * 为新消息创建 UserMessage（用于发送消息时）
     */
    @Transactional
    public void createUserMessagesForMessage(Message message, Long realmId) {
        // TODO: 为所有应该看到这条消息的用户创建 UserMessage
        // 逻辑：
        // 1. 如果是 Stream 消息，为订阅该 Stream 的所有用户创建
        // 2. 如果是私信，为所有收件人创建
        // 3. 如果消息 @提及了某用户，设置 MENTIONED flag
    }
}
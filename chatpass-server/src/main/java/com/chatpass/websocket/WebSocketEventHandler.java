package com.chatpass.websocket;

import com.chatpass.dto.MessageDTO;
import com.chatpass.dto.WebSocketDTO;
import com.chatpass.entity.*;
import com.chatpass.repository.UserProfileRepository;
import com.chatpass.service.MessageService;
import com.chatpass.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WebSocket 事件处理器
 * 
 * 负责将消息事件推送给客户端
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserMessageService userMessageService;
    private final UserProfileRepository userRepository;

    /**
     * 新消息事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNewMessage(MessageEvent event) {
        Message message = event.getMessage();
        Long realmId = message.getRealm().getId();

        WebSocketDTO.MessageEvent wsEvent = WebSocketDTO.MessageEvent.builder()
                .type("message")
                .id(generateEventId())
                .message(toResponse(message))
                .timestamp(LocalDateTime.now())
                .build();

        if (message.getIsChannelMessage()) {
            // Stream 消息 - 广播到 Stream Topic
            Long streamId = message.getRecipient().getStreamId();
            String topic = message.getSubject();

            // 推送到 Stream 订阅者
            messagingTemplate.convertAndSend(
                    "/topic/realm/" + realmId + "/stream/" + streamId,
                    wsEvent);

            // 也推送到 Topic
            if (topic != null && !topic.isEmpty()) {
                messagingTemplate.convertAndSend(
                        "/topic/realm/" + realmId + "/stream/" + streamId + "/topic/" + encodeTopic(topic),
                        wsEvent);
            }

            log.debug("Broadcast message {} to stream {} topic {}", 
                    message.getId(), streamId, topic);
        } else {
            // 私信 - 推送给特定用户
            // Recipient 记录了私信的目标信息
            // Zulip 的私信通过 Recipient 关联多个用户
            // 简化实现：推送给发送者和 realm 的活跃用户
            
            messagingTemplate.convertAndSendToUser(
                    message.getSender().getId().toString(),
                    "/queue/private",
                    wsEvent);
            
            log.debug("Send private message {} to sender {}", message.getId(), message.getSender().getId());
        }
    }

    /**
     * 消息更新事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageUpdate(MessageUpdateEvent event) {
        WebSocketDTO.UpdateMessageEvent wsEvent = WebSocketDTO.UpdateMessageEvent.builder()
                .type("update_message")
                .id(generateEventId())
                .messageId(event.getMessageId())
                .renderedContent(event.getNewContent())
                .subject(event.getNewTopic())
                .prevSubject(event.getOldTopic())
                .editTimestamp(System.currentTimeMillis())
                .userId(event.getUserId())
                .build();

        Long realmId = event.getRealmId();
        Long streamId = event.getStreamId();

        if (streamId != null) {
            // 广播更新
            messagingTemplate.convertAndSend(
                    "/topic/realm/" + realmId + "/stream/" + streamId,
                    wsEvent);
        } else {
            // 私信更新
            messagingTemplate.convertAndSend(
                    "/topic/realm/" + realmId + "/private",
                    wsEvent);
        }

        log.debug("Broadcast message update for message {}", event.getMessageId());
    }

    /**
     * 消息删除事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageDelete(MessageDeleteEvent event) {
        WebSocketDTO.DeleteMessageEvent wsEvent = WebSocketDTO.DeleteMessageEvent.builder()
                .type("delete_message")
                .id(generateEventId())
                .messageId(event.getMessageId())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/realm/" + event.getRealmId(),
                wsEvent);

        log.debug("Broadcast message delete for message {}", event.getMessageId());
    }

    /**
     * 已读状态更新事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRead(ReadEvent event) {
        WebSocketDTO.ReadEvent wsEvent = WebSocketDTO.ReadEvent.builder()
                .type("read")
                .id(generateEventId())
                .messageIds(event.getMessageIds())
                .build();

        // 只推送给该用户
        messagingTemplate.convertAndSendToUser(
                event.getUserId().toString(),
                "/queue/read",
                wsEvent);

        log.debug("Send read event to user {} for {} messages", 
                event.getUserId(), event.getMessageIds().size());
    }

    /**
     * 标记事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFlag(FlagEvent event) {
        WebSocketDTO.FlagEvent wsEvent = WebSocketDTO.FlagEvent.builder()
                .type(event.isAdd() ? "starred" : "unstarred")
                .id(generateEventId())
                .messageIds(event.getMessageIds())
                .flag(event.getFlag())
                .all(event.isAll())
                .build();

        messagingTemplate.convertAndSendToUser(
                event.getUserId().toString(),
                "/queue/flags",
                wsEvent);

        log.debug("Send flag event to user {}: {} {}", 
                event.getUserId(), event.getFlag(), event.isAdd() ? "added" : "removed");
    }

    /**
     * 发送心跳
     */
    public void sendHeartbeat(Long userId) {
        WebSocketDTO.HeartbeatEvent event = WebSocketDTO.HeartbeatEvent.builder()
                .type("heartbeat")
                .id(generateEventId())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/heartbeat",
                event);
    }

    /**
     * 发送用户状态
     */
    public void sendPresence(Long userId, String status) {
        WebSocketDTO.PresenceEvent event = WebSocketDTO.PresenceEvent.builder()
                .type("presence")
                .id(generateEventId())
                .userId(userId)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();

        // 广播用户状态（可选）
        messagingTemplate.convertAndSend(
                "/topic/presence",
                event);
    }

    // ==================== 内部事件类 ====================

    /**
     * 新消息事件
     */
    public static class MessageEvent {
        private Message message;
        private List<Long> targetUserIds;

        public MessageEvent(Message message, List<Long> targetUserIds) {
            this.message = message;
            this.targetUserIds = targetUserIds;
        }

        public Message getMessage() { return message; }
        public List<Long> getTargetUserIds() { return targetUserIds; }
    }

    /**
     * 消息更新事件
     */
    public static class MessageUpdateEvent {
        private Long messageId;
        private Long realmId;
        private Long streamId;
        private Long userId;
        private String newContent;
        private String newTopic;
        private String oldTopic;

        public MessageUpdateEvent(Long messageId, Long realmId, Long streamId, 
                Long userId, String newContent, String newTopic, String oldTopic) {
            this.messageId = messageId;
            this.realmId = realmId;
            this.streamId = streamId;
            this.userId = userId;
            this.newContent = newContent;
            this.newTopic = newTopic;
            this.oldTopic = oldTopic;
        }

        public Long getMessageId() { return messageId; }
        public Long getRealmId() { return realmId; }
        public Long getStreamId() { return streamId; }
        public Long getUserId() { return userId; }
        public String getNewContent() { return newContent; }
        public String getNewTopic() { return newTopic; }
        public String getOldTopic() { return oldTopic; }
    }

    /**
     * 消息删除事件
     */
    public static class MessageDeleteEvent {
        private Long messageId;
        private Long realmId;

        public MessageDeleteEvent(Long messageId, Long realmId) {
            this.messageId = messageId;
            this.realmId = realmId;
        }

        public Long getMessageId() { return messageId; }
        public Long getRealmId() { return realmId; }
    }

    /**
     * 已读事件
     */
    public static class ReadEvent {
        private Long userId;
        private List<Long> messageIds;

        public ReadEvent(Long userId, List<Long> messageIds) {
            this.userId = userId;
            this.messageIds = messageIds;
        }

        public Long getUserId() { return userId; }
        public List<Long> getMessageIds() { return messageIds; }
    }

    /**
     * 标记事件
     */
    public static class FlagEvent {
        private Long userId;
        private List<Long> messageIds;
        private String flag;
        private boolean add;
        private boolean all;

        public FlagEvent(Long userId, List<Long> messageIds, String flag, boolean add, boolean all) {
            this.userId = userId;
            this.messageIds = messageIds;
            this.flag = flag;
            this.add = add;
            this.all = all;
        }

        public Long getUserId() { return userId; }
        public List<Long> getMessageIds() { return messageIds; }
        public String getFlag() { return flag; }
        public boolean isAdd() { return add; }
        public boolean isAll() { return all; }
    }

    // ==================== 辅助方法 ====================

    private long eventIdCounter = 0;

    private Long generateEventId() {
        return ++eventIdCounter;
    }

    private String encodeTopic(String topic) {
        // URL 编码 Topic 名称
        return topic.replace("/", "_").replace(" ", "_");
    }

    private MessageDTO.Response toResponse(Message message) {
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
                .dateSent(message.getDateSent())
                .build();
    }
}
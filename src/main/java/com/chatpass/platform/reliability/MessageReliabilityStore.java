package com.chatpass.platform.reliability;

import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.output.OutboundMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class MessageReliabilityStore {

    private static final String IDEMPOTENCY_PREFIX = "chatpass:idempotency:";
    private static final String MESSAGE_STATE_PREFIX = "chatpass:message:state:";
    private static final String OUTBOX_PREFIX = "chatpass:outbox:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageReliabilityStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean tryAcquire(UnifiedMessage message) {
        Instant now = Instant.now();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(IDEMPOTENCY_PREFIX + idempotencyKey(message), message.getMessageId());
        if (!Boolean.TRUE.equals(acquired)) {
            return false;
        }
        redisTemplate.opsForHash().putAll(
            MESSAGE_STATE_PREFIX + message.getMessageId(),
            Map.of(
                "messageId", message.getMessageId(),
                "channel", message.getChannel().name(),
                "senderId", nullToEmpty(message.getSenderId()),
                "conversationId", nullToEmpty(message.getConversationId()),
                "status", "RECEIVED",
                "receivedAt", now.toString(),
                "updatedAt", now.toString()
            )
        );
        return true;
    }

    public void markStatus(String messageId, String status, String errorMessage) {
        String key = MESSAGE_STATE_PREFIX + messageId;
        redisTemplate.opsForHash().put(key, "status", status);
        redisTemplate.opsForHash().put(key, "errorMessage", nullToEmpty(errorMessage));
        redisTemplate.opsForHash().put(key, "updatedAt", Instant.now().toString());
    }

    public void recordOutbox(String sourceMessageId, OutboundMessage outboundMessage) {
        Instant now = Instant.now();
        redisTemplate.opsForHash().putAll(
            OUTBOX_PREFIX + outboundMessage.getMessageId(),
            Map.of(
                "deliveryId", outboundMessage.getMessageId(),
                "messageId", sourceMessageId,
                "channel", outboundMessage.getChannel().name(),
                "targetUserId", nullToEmpty(outboundMessage.getTargetUserId()),
                "payload", toJson(outboundMessage),
                "status", "PENDING",
                "attempts", "0",
                "createdAt", now.toString(),
                "updatedAt", now.toString()
            )
        );
    }

    private String idempotencyKey(UnifiedMessage message) {
        return message.getChannel().name() + ":" + message.getMessageId();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize reliability payload", ex);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

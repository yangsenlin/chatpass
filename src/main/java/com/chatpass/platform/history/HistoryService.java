package com.chatpass.platform.history;

import com.chatpass.platform.message.MessageProcessingResult;
import com.chatpass.platform.message.UnifiedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class HistoryService {

    private static final String TENANT_PREFIX = "chatpass:tenant:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public HistoryService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void record(MessageProcessingResult result) {
        UnifiedMessage message = result.getMessage();
        String tenantId = tenantId(message);
        String conversationId = conversationId(message);
        long now = Instant.now().toEpochMilli();
        redisTemplate.opsForZSet().add(conversationKey(tenantId), conversationId, now);
        redisTemplate.opsForList().rightPush(messageKey(tenantId, conversationId), toJson(result));
    }

    public List<String> conversations(String tenantId, int offset, int limit) {
        Long end = (long) offset + limit - 1;
        Set<String> conversations = redisTemplate.opsForZSet().reverseRange(conversationKey(tenantId), offset, end);
        if (conversations == null) {
            return List.of();
        }
        return conversations
            .stream()
            .filter(Objects::nonNull)
            .toList();
    }

    public List<Object> messages(String tenantId, String conversationId, int offset, int limit) {
        long end = (long) offset + limit - 1;
        List<String> payloads = redisTemplate.opsForList().range(messageKey(tenantId, conversationId), offset, end);
        if (payloads == null) {
            return List.of();
        }
        return payloads.stream().map(this::fromJson).toList();
    }

    private String tenantId(UnifiedMessage message) {
        if (message.getTenantId() != null && !message.getTenantId().isBlank()) {
            return message.getTenantId();
        }
        Object tenant = message.getAttributes().get("tenantId");
        return tenant == null ? "default" : String.valueOf(tenant);
    }

    private String conversationId(UnifiedMessage message) {
        return message.getConversationId() == null ? message.getSenderId() : message.getConversationId();
    }

    private String conversationKey(String tenantId) {
        return TENANT_PREFIX + tenantId + ":conversations";
    }

    private String messageKey(String tenantId, String conversationId) {
        return TENANT_PREFIX + tenantId + ":conversation:" + conversationId + ":messages";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize history record", ex);
        }
    }

    private Object fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize history record", ex);
        }
    }
}

package com.chatpass.platform.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class RedisStreamStore {

    private static final String STREAM_PREFIX = "chatpass:stream:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisStreamStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void create(StreamMessage startMessage) {
        redisTemplate.opsForHash().putAll(stateKey(startMessage.getStreamId()), Map.of(
            "streamId", startMessage.getStreamId(),
            "tenantId", nullToEmpty(startMessage.getTenantId()),
            "appId", nullToEmpty(startMessage.getAppId()),
            "conversationId", nullToEmpty(startMessage.getConversationId()),
            "messageId", nullToEmpty(startMessage.getMessageId()),
            "state", StreamState.STREAMING.name(),
            "createdAt", Instant.now().toString(),
            "updatedAt", Instant.now().toString()
        ));
    }

    public void append(StreamMessage chunk) {
        redisTemplate.opsForList().rightPush(chunksKey(chunk.getStreamId()), toJson(chunk));
        redisTemplate.opsForValue().append(contentKey(chunk.getStreamId()), chunk.getContent() == null ? "" : chunk.getContent());
        redisTemplate.opsForHash().put(stateKey(chunk.getStreamId()), "updatedAt", Instant.now().toString());
    }

    public void complete(String streamId) {
        redisTemplate.opsForHash().put(stateKey(streamId), "state", StreamState.COMPLETED.name());
        redisTemplate.opsForHash().put(stateKey(streamId), "updatedAt", Instant.now().toString());
    }

    public void fail(String streamId, String errorMessage) {
        redisTemplate.opsForHash().put(stateKey(streamId), "state", StreamState.FAILED.name());
        redisTemplate.opsForHash().put(stateKey(streamId), "errorMessage", nullToEmpty(errorMessage));
        redisTemplate.opsForHash().put(stateKey(streamId), "updatedAt", Instant.now().toString());
    }

    public void cancel(String streamId) {
        redisTemplate.opsForHash().put(stateKey(streamId), "state", StreamState.CANCELLED.name());
        redisTemplate.opsForHash().put(stateKey(streamId), "updatedAt", Instant.now().toString());
    }

    public boolean isCancelled(String streamId) {
        Object state = redisTemplate.opsForHash().get(stateKey(streamId), "state");
        return StreamState.CANCELLED.name().equals(state);
    }

    public List<StreamMessage> chunksAfter(String streamId, int lastSequence) {
        List<String> payloads = redisTemplate.opsForList().range(chunksKey(streamId), 0, -1);
        if (payloads == null) {
            return List.of();
        }
        return payloads.stream()
            .filter(Objects::nonNull)
            .map(this::fromJson)
            .filter(message -> message.getSequence() > lastSequence)
            .toList();
    }

    public String content(String streamId) {
        String content = redisTemplate.opsForValue().get(contentKey(streamId));
        return content == null ? "" : content;
    }

    public Map<Object, Object> state(String streamId) {
        return redisTemplate.opsForHash().entries(stateKey(streamId));
    }

    private String stateKey(String streamId) {
        return STREAM_PREFIX + streamId + ":state";
    }

    private String chunksKey(String streamId) {
        return STREAM_PREFIX + streamId + ":chunks";
    }

    private String contentKey(String streamId) {
        return STREAM_PREFIX + streamId + ":content";
    }

    private String toJson(StreamMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize stream message", ex);
        }
    }

    private StreamMessage fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, StreamMessage.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize stream message", ex);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

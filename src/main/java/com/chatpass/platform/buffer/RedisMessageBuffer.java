package com.chatpass.platform.buffer;

import com.chatpass.platform.message.MessageIngressService;
import com.chatpass.platform.message.UnifiedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Primary
@Component
public class RedisMessageBuffer implements MessageBuffer {

    private static final int MAX_ATTEMPTS = 3;
    private static final String QUEUE_KEY = "chatpass:queue:messages";
    private static final String PROCESSING_KEY = "chatpass:queue:messages:processing";
    private static final String DLQ_KEY = "chatpass:queue:dead-letter";
    private static final String BUFFER_PREFIX = "chatpass:buffer:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MessageIngressService messageIngressService;
    private ExecutorService worker;
    private volatile boolean running;

    public RedisMessageBuffer(
        StringRedisTemplate redisTemplate,
        ObjectMapper objectMapper,
        MessageIngressService messageIngressService
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.messageIngressService = messageIngressService;
    }

    @PostConstruct
    public void start() {
        running = true;
        recoverProcessingMessages();
        worker = Executors.newSingleThreadExecutor();
        worker.submit(this::consume);
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (worker != null) {
            worker.shutdownNow();
        }
    }

    @Override
    public BufferedMessage enqueue(UnifiedMessage message) {
        BufferedMessage bufferedMessage = new BufferedMessage(message);
        String payload = toJson(Map.of("bufferId", bufferedMessage.getBufferId(), "message", message));
        redisTemplate.opsForHash().putAll(
            BUFFER_PREFIX + bufferedMessage.getBufferId(),
            Map.of(
                "bufferId", bufferedMessage.getBufferId(),
                "messageId", message.getMessageId(),
                "status", "PENDING",
                "attempts", "0",
                "receivedAt", Instant.now().toString(),
                "updatedAt", Instant.now().toString()
            )
        );
        redisTemplate.opsForList().leftPush(QUEUE_KEY, payload);
        return bufferedMessage;
    }

    @Override
    public Optional<Object> result(String bufferId) {
        Map<Object, Object> status = redisTemplate.opsForHash().entries(BUFFER_PREFIX + bufferId);
        return status.isEmpty() ? Optional.empty() : Optional.of(status);
    }

    @Override
    public int size() {
        Long size = redisTemplate.opsForList().size(QUEUE_KEY);
        return size == null ? 0 : size.intValue();
    }

    private void consume() {
        while (running) {
            String payload = redisTemplate.opsForList().rightPopAndLeftPush(QUEUE_KEY, PROCESSING_KEY, Duration.ofSeconds(1));
            if (payload == null) {
                continue;
            }
            process(payload);
        }
    }

    private void recoverProcessingMessages() {
        String payload;
        while ((payload = redisTemplate.opsForList().rightPop(PROCESSING_KEY)) != null) {
            redisTemplate.opsForList().leftPush(QUEUE_KEY, payload);
        }
    }

    private void process(String payload) {
        try {
            QueuePayload queuePayload = objectMapper.readValue(payload, QueuePayload.class);
            mark(queuePayload.bufferId(), "PROCESSING", null);
            messageIngressService.receive(queuePayload.message());
            mark(queuePayload.bufferId(), "DONE", null);
            ack(payload);
        } catch (Exception ex) {
            retryOrDeadLetter(payload, ex.getMessage());
        }
    }

    private void retryOrDeadLetter(String payload, String reason) {
        try {
            QueuePayload queuePayload = objectMapper.readValue(payload, QueuePayload.class);
            String key = BUFFER_PREFIX + queuePayload.bufferId();
            int attempts = Integer.parseInt(String.valueOf(redisTemplate.opsForHash().get(key, "attempts"))) + 1;
            redisTemplate.opsForHash().put(key, "attempts", String.valueOf(attempts));
            redisTemplate.opsForHash().put(key, "errorMessage", reason == null ? "" : reason);
            redisTemplate.opsForHash().put(key, "updatedAt", Instant.now().toString());
            redisTemplate.opsForHash().put(key, "status", attempts >= MAX_ATTEMPTS ? "DEAD" : "RETRY");
            ack(payload);
            if (attempts < MAX_ATTEMPTS) {
                redisTemplate.opsForList().leftPush(QUEUE_KEY, payload);
            } else {
                redisTemplate.opsForList().leftPush(DLQ_KEY, payload);
            }
        } catch (Exception ignored) {
            ack(payload);
            redisTemplate.opsForList().leftPush(DLQ_KEY, payload);
        }
    }

    private void ack(String payload) {
        redisTemplate.opsForList().remove(PROCESSING_KEY, 1, payload);
    }

    private void mark(String bufferId, String status, String errorMessage) {
        String key = BUFFER_PREFIX + bufferId;
        redisTemplate.opsForHash().put(key, "status", status);
        redisTemplate.opsForHash().put(key, "updatedAt", Instant.now().toString());
        if (errorMessage != null) {
            redisTemplate.opsForHash().put(key, "errorMessage", errorMessage);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize Redis queue payload", ex);
        }
    }

    private record QueuePayload(String bufferId, UnifiedMessage message) {
    }
}

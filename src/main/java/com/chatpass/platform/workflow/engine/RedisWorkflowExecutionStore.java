package com.chatpass.platform.workflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Primary
@Component
public class RedisWorkflowExecutionStore implements WorkflowExecutionStore {

    private static final String EXECUTION_PREFIX = "chatpass:workflow:execution:";
    private static final String NODE_RESULT_PREFIX = "chatpass:workflow:node-result:";

    private final Map<String, WorkflowExecutionContext> liveContexts = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisWorkflowExecutionStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(WorkflowExecutionContext context) {
        liveContexts.put(context.getExecutionId(), context);
        redisTemplate.opsForHash().putAll(
            EXECUTION_PREFIX + context.getExecutionId(),
            Map.of(
                "executionId", context.getExecutionId(),
                "workflowId", context.getDefinition().getId(),
                "status", context.getStatus().name(),
                "variables", toJson(context.variables()),
                "updatedAt", Instant.now().toString()
            )
        );
        context.variables().forEach((key, value) -> {
            int dotIndex = key.indexOf('.');
            if (dotIndex > 0) {
                redisTemplate.opsForHash().put(
                    NODE_RESULT_PREFIX + context.getExecutionId(),
                    key.substring(0, dotIndex),
                    toJson(Map.of(key.substring(dotIndex + 1), value))
                );
            }
        });
    }

    @Override
    public Optional<WorkflowExecutionContext> findById(String executionId) {
        return Optional.ofNullable(liveContexts.get(executionId));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize workflow execution", ex);
        }
    }
}

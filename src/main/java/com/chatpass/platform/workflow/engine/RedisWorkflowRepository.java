package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.workflow.WorkflowType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
@Repository
public class RedisWorkflowRepository implements WorkflowRepository {

    private static final String WORKFLOW_KEY = "chatpass:workflow:definitions";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final WorkflowValidator workflowValidator;

    public RedisWorkflowRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, WorkflowValidator workflowValidator) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.workflowValidator = workflowValidator;
    }

    @PostConstruct
    public void initDefaultWorkflow() {
        if (findById("default-auto-reply").isPresent()) {
            return;
        }
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("default-auto-reply");
        definition.setName("Default auto reply workflow");
        WorkflowNodeDefinition start = node("start", NodeType.START);
        WorkflowNodeDefinition reply = node("reply", NodeType.ACTION);
        reply.setActionType(WorkflowType.AUTO_REPLY);
        reply.setParameters(Map.of("text", "Message accepted by ChatPass workflow."));
        WorkflowNodeDefinition end = node("end", NodeType.END);
        definition.setNodes(List.of(start, reply, end));
        definition.setEdges(List.of(edge("start-reply", "start", "reply"), edge("reply-end", "reply", "end")));
        save(definition);
    }

    @Override
    public List<WorkflowDefinition> findAll() {
        return redisTemplate.opsForHash().values(WORKFLOW_KEY).stream()
            .map(value -> fromJson(String.valueOf(value)))
            .toList();
    }

    @Override
    public Optional<WorkflowDefinition> findById(String id) {
        Object value = redisTemplate.opsForHash().get(WORKFLOW_KEY, id);
        return value == null ? Optional.empty() : Optional.of(fromJson(String.valueOf(value)));
    }

    @Override
    public WorkflowDefinition save(WorkflowDefinition definition) {
        workflowValidator.validate(definition);
        redisTemplate.opsForHash().put(WORKFLOW_KEY, definition.getId(), toJson(definition));
        return definition;
    }

    @Override
    public void deleteById(String id) {
        redisTemplate.opsForHash().delete(WORKFLOW_KEY, id);
    }

    private WorkflowNodeDefinition node(String id, NodeType type) {
        WorkflowNodeDefinition node = new WorkflowNodeDefinition();
        node.setId(id);
        node.setName(id);
        node.setType(type);
        return node;
    }

    private WorkflowEdgeDefinition edge(String id, String from, String to) {
        WorkflowEdgeDefinition edge = new WorkflowEdgeDefinition();
        edge.setId(id);
        edge.setFrom(from);
        edge.setTo(to);
        return edge;
    }

    private WorkflowDefinition fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, WorkflowDefinition.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize workflow definition", ex);
        }
    }

    private String toJson(WorkflowDefinition definition) {
        try {
            return objectMapper.writeValueAsString(definition);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize workflow definition", ex);
        }
    }
}

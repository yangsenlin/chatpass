package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.message.UnifiedMessage;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorkflowExecutionContext {

    private final String executionId = UUID.randomUUID().toString();
    private final WorkflowDefinition definition;
    private final UnifiedMessage message;
    private final Map<String, Object> variables = new LinkedHashMap<>();
    private final Map<String, Set<String>> triggeredEdgesByNode = new LinkedHashMap<>();
    private final Instant startedAt = Instant.now();
    private WorkflowStatus status = WorkflowStatus.READY;
    private String suspendedNodeId;
    private String errorMessage;

    public WorkflowExecutionContext(WorkflowDefinition definition, UnifiedMessage message, Map<String, Object> inputVariables) {
        this.definition = definition;
        this.message = message;
        if (inputVariables != null) {
            variables.putAll(inputVariables);
        }
        put("message.id", message.getMessageId());
        put("message.channel", message.getChannel());
        put("message.senderId", message.getSenderId());
        put("message.receiverId", message.getReceiverId());
        put("message.conversationId", message.getConversationId());
        put("message.text", message.getText());
    }

    public WorkflowDefinition getDefinition() {
        return definition;
    }

    public UnifiedMessage getMessage() {
        return message;
    }

    public String getExecutionId() {
        return executionId;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public String getSuspendedNodeId() {
        return suspendedNodeId;
    }

    public void setSuspendedNodeId(String suspendedNodeId) {
        this.suspendedNodeId = suspendedNodeId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Map<String, Object> variables() {
        return variables;
    }

    public Object get(String key) {
        return variables.get(key);
    }

    public void put(String key, Object value) {
        if (key != null && value != null) {
            variables.put(key, value);
        }
    }

    public void putNodeOutputs(String nodeId, Map<String, Object> outputs) {
        if (outputs == null) {
            return;
        }
        outputs.forEach((key, value) -> put(nodeId + "." + key, value));
    }

    public void recordTrigger(String nodeId, String edgeId) {
        triggeredEdgesByNode.computeIfAbsent(nodeId, ignored -> new LinkedHashSet<>()).add(edgeId);
    }

    public int triggeredCount(String nodeId) {
        return triggeredEdgesByNode.getOrDefault(nodeId, Set.of()).size();
    }
}

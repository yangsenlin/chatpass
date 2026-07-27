package com.chatpass.platform.workflow.mojarvis;

import java.time.Instant;

public class WorkflowEvent {

    private final WorkflowEventType type;
    private final String workflowId;
    private final String nodeId;
    private final Instant createdAt = Instant.now();
    private final String message;

    public WorkflowEvent(WorkflowEventType type, String workflowId, String nodeId, String message) {
        this.type = type;
        this.workflowId = workflowId;
        this.nodeId = nodeId;
        this.message = message;
    }

    public WorkflowEventType getType() {
        return type;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }
}

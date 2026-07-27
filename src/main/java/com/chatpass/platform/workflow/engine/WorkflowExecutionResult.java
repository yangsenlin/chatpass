package com.chatpass.platform.workflow.engine;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowExecutionResult {

    private String executionId;

    private String workflowId;

    private WorkflowStatus status;

    private String suspendedNodeId;

    private Map<String, Object> variables = new LinkedHashMap<>();

    private String errorMessage;

    private Instant startedAt;

    private Instant finishedAt;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
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

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables == null ? new LinkedHashMap<>() : variables;
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

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}

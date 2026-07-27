package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.message.UnifiedMessage;

import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowExecutionRequest {

    private String workflowId;

    private UnifiedMessage message;

    private Map<String, Object> variables = new LinkedHashMap<>();

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public UnifiedMessage getMessage() {
        return message;
    }

    public void setMessage(UnifiedMessage message) {
        this.message = message;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables == null ? new LinkedHashMap<>() : variables;
    }
}

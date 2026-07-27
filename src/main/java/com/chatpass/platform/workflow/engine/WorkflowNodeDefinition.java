package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.workflow.WorkflowType;

import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowNodeDefinition {

    private String id;

    private String name;

    private NodeType type = NodeType.ACTION;

    private WorkflowType actionType;

    private boolean async;

    private NodeTriggerStrategy triggerStrategy = NodeTriggerStrategy.ANY;

    private Map<String, Object> parameters = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public WorkflowType getActionType() {
        return actionType;
    }

    public void setActionType(WorkflowType actionType) {
        this.actionType = actionType;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public NodeTriggerStrategy getTriggerStrategy() {
        return triggerStrategy;
    }

    public void setTriggerStrategy(NodeTriggerStrategy triggerStrategy) {
        this.triggerStrategy = triggerStrategy;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters == null ? new LinkedHashMap<>() : parameters;
    }
}

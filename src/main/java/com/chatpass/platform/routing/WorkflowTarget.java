package com.chatpass.platform.routing;

import com.chatpass.platform.workflow.WorkflowType;

import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowTarget {

    private WorkflowType type = WorkflowType.NOOP;

    private String target;

    private Map<String, Object> parameters = new LinkedHashMap<>();

    public WorkflowType getType() {
        return type;
    }

    public void setType(WorkflowType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters == null ? new LinkedHashMap<>() : parameters;
    }
}

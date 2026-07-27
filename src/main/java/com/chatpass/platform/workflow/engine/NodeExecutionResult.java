package com.chatpass.platform.workflow.engine;

import java.util.LinkedHashMap;
import java.util.Map;

public class NodeExecutionResult {

    private boolean suspended;

    private Map<String, Object> outputs = new LinkedHashMap<>();

    public static NodeExecutionResult completed(Map<String, Object> outputs) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setOutputs(outputs);
        return result;
    }

    public static NodeExecutionResult suspended(Map<String, Object> outputs) {
        NodeExecutionResult result = completed(outputs);
        result.setSuspended(true);
        return result;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs == null ? new LinkedHashMap<>() : outputs;
    }
}

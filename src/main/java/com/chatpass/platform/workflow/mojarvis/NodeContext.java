package com.chatpass.platform.workflow.mojarvis;

import java.util.LinkedHashMap;
import java.util.Map;

public class NodeContext {

    private final String nodeId;
    private WorkflowNodeStatus status = WorkflowNodeStatus.READY;
    private Map<String, Object> outputs = new LinkedHashMap<>();

    public NodeContext(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public WorkflowNodeStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowNodeStatus status) {
        this.status = status;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs == null ? new LinkedHashMap<>() : outputs;
    }
}

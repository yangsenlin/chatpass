package com.chatpass.platform.workflow.mojarvis;

public class WorkflowSuspendException extends RuntimeException {

    private final String nodeId;

    public WorkflowSuspendException(String nodeId) {
        super("Workflow suspended at node: " + nodeId);
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}

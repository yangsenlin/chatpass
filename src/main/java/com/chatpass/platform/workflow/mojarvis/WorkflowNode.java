package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.NodeTriggerStrategy;

import java.util.ArrayList;
import java.util.List;

public abstract class WorkflowNode {

    protected String id;
    protected String name;
    protected boolean async;
    protected List<WorkflowEdge> inwardEdges = new ArrayList<>();
    protected List<WorkflowEdge> outwardEdges = new ArrayList<>();
    protected WorkflowNodeStatus nodeStatus = WorkflowNodeStatus.READY;
    protected NodeTriggerStrategy triggerStrategy = NodeTriggerStrategy.ANY;

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

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public List<WorkflowEdge> getInwardEdges() {
        return inwardEdges;
    }

    public List<WorkflowEdge> getOutwardEdges() {
        return outwardEdges;
    }

    public WorkflowNodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(WorkflowNodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public NodeTriggerStrategy getTriggerStrategy() {
        return triggerStrategy;
    }

    public void setTriggerStrategy(NodeTriggerStrategy triggerStrategy) {
        this.triggerStrategy = triggerStrategy == null ? NodeTriggerStrategy.ANY : triggerStrategy;
    }

    protected abstract NodeExecutionResult execute(Workflow workflow);
}

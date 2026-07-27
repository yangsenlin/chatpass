package com.chatpass.platform.workflow.engine;

import java.util.ArrayList;
import java.util.List;

public class WorkflowDefinition {

    private String id;

    private String name;

    private boolean enabled = true;

    private List<WorkflowNodeDefinition> nodes = new ArrayList<>();

    private List<WorkflowEdgeDefinition> edges = new ArrayList<>();

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<WorkflowNodeDefinition> getNodes() {
        return nodes;
    }

    public void setNodes(List<WorkflowNodeDefinition> nodes) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
    }

    public List<WorkflowEdgeDefinition> getEdges() {
        return edges;
    }

    public void setEdges(List<WorkflowEdgeDefinition> edges) {
        this.edges = edges == null ? new ArrayList<>() : edges;
    }
}

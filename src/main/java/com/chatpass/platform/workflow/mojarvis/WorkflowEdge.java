package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.routing.RuleCondition;

import java.util.ArrayList;
import java.util.List;

public class WorkflowEdge {

    private String id;
    private String source;
    private String target;
    private List<RuleCondition> conditions = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }
}

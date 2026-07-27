package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.routing.RuleCondition;

import java.util.ArrayList;
import java.util.List;

public class WorkflowEdgeDefinition {

    private String id;

    private String from;

    private String to;

    private List<RuleCondition> conditions = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }
}

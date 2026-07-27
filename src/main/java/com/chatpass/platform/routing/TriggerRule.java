package com.chatpass.platform.routing;

import java.util.ArrayList;
import java.util.List;

public class TriggerRule {

    private String id;

    private String name;

    private boolean enabled = true;

    private int priority;

    private ConditionGroupOperator conditionOperator = ConditionGroupOperator.ALL;

    private List<RuleCondition> conditions = new ArrayList<>();

    private WorkflowTarget workflow = new WorkflowTarget();

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ConditionGroupOperator getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(ConditionGroupOperator conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }

    public WorkflowTarget getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowTarget workflow) {
        this.workflow = workflow == null ? new WorkflowTarget() : workflow;
    }
}

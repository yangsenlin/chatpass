package com.chatpass.platform.routing;

import java.util.ArrayList;
import java.util.List;

public class RuleCondition {

    private String field;

    private MatchOperator operator = MatchOperator.EQUALS;

    private Object value;

    private List<Object> values = new ArrayList<>();

    private boolean ignoreCase = true;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public MatchOperator getOperator() {
        return operator;
    }

    public void setOperator(MatchOperator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values == null ? new ArrayList<>() : values;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
}

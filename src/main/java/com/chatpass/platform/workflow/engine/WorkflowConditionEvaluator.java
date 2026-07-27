package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.routing.MatchOperator;
import com.chatpass.platform.routing.RuleCondition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class WorkflowConditionEvaluator {

    public boolean matches(List<RuleCondition> conditions, WorkflowExecutionContext context) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        return conditions.stream().allMatch(condition -> matches(condition, context));
    }

    private boolean matches(RuleCondition condition, WorkflowExecutionContext context) {
        Object actual = context.get(condition.getField());
        MatchOperator operator = condition.getOperator() == null ? MatchOperator.EQUALS : condition.getOperator();

        switch (operator) {
            case EXISTS:
                return actual != null;
            case EQUALS:
                return normalize(actual, condition).equals(normalize(condition.getValue(), condition));
            case NOT_EQUALS:
                return !normalize(actual, condition).equals(normalize(condition.getValue(), condition));
            case CONTAINS:
                return normalize(actual, condition).contains(normalize(condition.getValue(), condition));
            case STARTS_WITH:
                return normalize(actual, condition).startsWith(normalize(condition.getValue(), condition));
            case ENDS_WITH:
                return normalize(actual, condition).endsWith(normalize(condition.getValue(), condition));
            case IN:
                return condition.getValues().stream()
                    .filter(Objects::nonNull)
                    .map(value -> normalize(value, condition))
                    .anyMatch(normalize(actual, condition)::equals);
            case REGEX:
                return actual != null && Pattern.compile(String.valueOf(condition.getValue())).matcher(String.valueOf(actual)).find();
            default:
                throw new IllegalStateException("Unsupported workflow condition operator: " + operator);
        }
    }

    private String normalize(Object value, RuleCondition condition) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        return condition.isIgnoreCase() ? text.toLowerCase(Locale.ROOT) : text;
    }
}

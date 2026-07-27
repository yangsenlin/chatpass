package com.chatpass.platform.routing;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class ConditionMatcher {

    public boolean matches(RuleCondition condition, TriggerContext context) {
        Object actual = context.get(condition.getField());
        MatchOperator operator = condition.getOperator();
        if (operator == null) {
            operator = MatchOperator.EQUALS;
        }

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
                return inValues(actual, condition);
            case REGEX:
                return actual != null && Pattern.compile(String.valueOf(condition.getValue())).matcher(String.valueOf(actual)).find();
            default:
                throw new IllegalStateException("Unsupported match operator: " + operator);
        }
    }

    private boolean inValues(Object actual, RuleCondition condition) {
        if (condition.getValues() == null || condition.getValues().isEmpty()) {
            return false;
        }
        String normalizedActual = normalize(actual, condition);
        return condition.getValues().stream()
            .filter(Objects::nonNull)
            .map(value -> normalize(value, condition))
            .anyMatch(normalizedActual::equals);
    }

    private String normalize(Object value, RuleCondition condition) {
        if (value == null) {
            return "";
        }
        String text;
        if (value instanceof Collection<?> collection) {
            text = collection.toString();
        } else {
            text = String.valueOf(value);
        }
        return condition.isIgnoreCase() ? text.toLowerCase(Locale.ROOT) : text;
    }
}

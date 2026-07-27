package com.chatpass.platform.routing;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class TriggerMatcher {

    private final ConditionMatcher conditionMatcher;

    public TriggerMatcher(ConditionMatcher conditionMatcher) {
        this.conditionMatcher = conditionMatcher;
    }

    public Optional<TriggerRule> firstMatch(List<TriggerRule> rules, TriggerContext context) {
        return rules.stream()
            .filter(TriggerRule::isEnabled)
            .sorted(Comparator.comparingInt(TriggerRule::getPriority).reversed())
            .filter(rule -> matches(rule, context))
            .findFirst();
    }

    public boolean matches(TriggerRule rule, TriggerContext context) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return true;
        }
        ConditionGroupOperator groupOperator = rule.getConditionOperator();
        if (groupOperator == null) {
            groupOperator = ConditionGroupOperator.ALL;
        }

        switch (groupOperator) {
            case ALL:
                return rule.getConditions().stream().allMatch(condition -> conditionMatcher.matches(condition, context));
            case ANY:
                return rule.getConditions().stream().anyMatch(condition -> conditionMatcher.matches(condition, context));
            default:
                throw new IllegalStateException("Unsupported condition group operator: " + groupOperator);
        }
    }
}

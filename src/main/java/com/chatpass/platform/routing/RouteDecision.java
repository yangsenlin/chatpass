package com.chatpass.platform.routing;

import java.time.Instant;
import java.util.Optional;

public class RouteDecision {

    private final String decisionId;
    private final Instant decidedAt;
    private final TriggerRule rule;

    public RouteDecision(String decisionId, Instant decidedAt, TriggerRule rule) {
        this.decisionId = decisionId;
        this.decidedAt = decidedAt;
        this.rule = rule;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public Optional<TriggerRule> getRule() {
        return Optional.ofNullable(rule);
    }

    public boolean hasMatch() {
        return rule != null;
    }
}

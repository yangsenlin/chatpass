package com.chatpass.platform.workflow;

import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.routing.RouteDecision;
import com.chatpass.platform.routing.TriggerRule;

public class ActionExecutionRequest {

    private final UnifiedMessage message;
    private final RouteDecision decision;
    private final TriggerRule rule;

    public ActionExecutionRequest(UnifiedMessage message, RouteDecision decision, TriggerRule rule) {
        this.message = message;
        this.decision = decision;
        this.rule = rule;
    }

    public UnifiedMessage getMessage() {
        return message;
    }

    public RouteDecision getDecision() {
        return decision;
    }

    public TriggerRule getRule() {
        return rule;
    }
}

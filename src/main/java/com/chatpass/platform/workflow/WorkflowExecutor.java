package com.chatpass.platform.workflow;

import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.routing.RouteDecision;
import com.chatpass.platform.routing.TriggerRule;
import org.springframework.stereotype.Service;

@Service
public class WorkflowExecutor {

    private final ActionRegistry actionRegistry;

    public WorkflowExecutor(ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    public ActionResult execute(UnifiedMessage message, RouteDecision decision) {
        TriggerRule rule = decision.getRule().orElse(null);
        if (rule == null || rule.getWorkflow() == null) {
            return ActionResult.skipped("No route rule matched");
        }

        WorkflowType type = rule.getWorkflow().getType();
        return actionRegistry.find(type)
            .map(action -> action.execute(new ActionExecutionRequest(message, decision, rule)))
            .orElseGet(() -> ActionResult.failed("No action registered for workflow type: " + type));
    }
}

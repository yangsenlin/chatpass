package com.chatpass.platform.workflow.engine.node;

import com.chatpass.platform.routing.RouteDecision;
import com.chatpass.platform.routing.TriggerRule;
import com.chatpass.platform.routing.WorkflowTarget;
import com.chatpass.platform.workflow.ActionExecutionRequest;
import com.chatpass.platform.workflow.ActionRegistry;
import com.chatpass.platform.workflow.ActionResult;
import com.chatpass.platform.workflow.WorkflowAction;
import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.NodeType;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ActionNodeExecutor implements WorkflowNodeExecutor {

    private final ActionRegistry actionRegistry;

    public ActionNodeExecutor(ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    @Override
    public NodeType type() {
        return NodeType.ACTION;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNodeDefinition node, WorkflowExecutionContext context) {
        WorkflowAction action = actionRegistry.find(node.getActionType())
            .orElseThrow(() -> new IllegalStateException("No action registered for node: " + node.getId()));

        TriggerRule rule = new TriggerRule();
        rule.setId(context.getDefinition().getId() + ":" + node.getId());
        rule.setName(node.getName());
        WorkflowTarget target = new WorkflowTarget();
        target.setType(node.getActionType());
        target.setTarget(node.getId());
        target.setParameters(node.getParameters());
        rule.setWorkflow(target);

        RouteDecision decision = new RouteDecision(context.getExecutionId(), Instant.now(), rule);
        ActionResult actionResult = action.execute(new ActionExecutionRequest(context.getMessage(), decision, rule));

        Map<String, Object> outputs = new LinkedHashMap<>(actionResult.getOutputs());
        outputs.put("status", actionResult.getStatus());
        outputs.put("outputText", actionResult.getOutputText());
        outputs.put("errorMessage", actionResult.getErrorMessage());
        return NodeExecutionResult.completed(outputs);
    }
}

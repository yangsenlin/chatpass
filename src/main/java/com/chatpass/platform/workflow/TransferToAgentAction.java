package com.chatpass.platform.workflow;

import org.springframework.stereotype.Component;

@Component
public class TransferToAgentAction implements WorkflowAction {

    @Override
    public WorkflowType type() {
        return WorkflowType.TRANSFER_TO_AGENT;
    }

    @Override
    public ActionResult execute(ActionExecutionRequest request) {
        String queue = String.valueOf(request.getRule().getWorkflow().getParameters().getOrDefault("queue", "default"));
        ActionResult result = ActionResult.success("Transfer requested");
        result.getOutputs().put("handoff", true);
        result.getOutputs().put("queue", queue);
        return result;
    }
}

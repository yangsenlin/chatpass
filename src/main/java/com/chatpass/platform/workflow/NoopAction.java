package com.chatpass.platform.workflow;

import org.springframework.stereotype.Component;

@Component
public class NoopAction implements WorkflowAction {

    @Override
    public WorkflowType type() {
        return WorkflowType.NOOP;
    }

    @Override
    public ActionResult execute(ActionExecutionRequest request) {
        return ActionResult.skipped("No operation workflow");
    }
}

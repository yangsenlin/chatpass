package com.chatpass.platform.workflow;

public interface WorkflowAction {

    WorkflowType type();

    ActionResult execute(ActionExecutionRequest request);
}

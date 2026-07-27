package com.chatpass.platform.workflow.mojarvis;

@FunctionalInterface
public interface WorkFlowEventListener {

    void onEvent(WorkflowEvent event, Workflow workflow);
}

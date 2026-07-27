package com.chatpass.platform.workflow.mojarvis;

public enum WorkflowEventType {
    WORKFLOW_START,
    WORKFLOW_END,
    WORKFLOW_STATUS_CHANGE,
    WORKFLOW_RESUME,
    NODE_START,
    NODE_END,
    NODE_ERROR,
    NODE_SUSPEND
}

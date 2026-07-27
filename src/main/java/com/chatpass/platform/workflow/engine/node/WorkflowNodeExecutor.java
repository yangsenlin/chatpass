package com.chatpass.platform.workflow.engine.node;

import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.NodeType;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;

public interface WorkflowNodeExecutor {

    NodeType type();

    NodeExecutionResult execute(WorkflowNodeDefinition node, WorkflowExecutionContext context);
}

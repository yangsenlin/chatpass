package com.chatpass.platform.workflow.engine.node;

import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.NodeType;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
public class ConditionNodeExecutor implements WorkflowNodeExecutor {

    @Override
    public NodeType type() {
        return NodeType.CONDITION;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNodeDefinition node, WorkflowExecutionContext context) {
        return NodeExecutionResult.completed(new LinkedHashMap<>(node.getParameters()));
    }
}

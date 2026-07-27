package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import com.chatpass.platform.workflow.engine.node.WorkflowNodeExecutorRegistry;

public class ConditionNode extends DefinitionNode {

    public ConditionNode(WorkflowNodeDefinition definition, WorkflowNodeExecutorRegistry executorRegistry) {
        super(definition, executorRegistry);
    }
}

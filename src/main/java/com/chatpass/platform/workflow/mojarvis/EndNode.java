package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import com.chatpass.platform.workflow.engine.node.WorkflowNodeExecutorRegistry;

public class EndNode extends DefinitionNode {

    public EndNode(WorkflowNodeDefinition definition, WorkflowNodeExecutorRegistry executorRegistry) {
        super(definition, executorRegistry);
    }
}

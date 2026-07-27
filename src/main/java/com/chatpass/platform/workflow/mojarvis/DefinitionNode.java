package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import com.chatpass.platform.workflow.engine.node.WorkflowNodeExecutorRegistry;

public abstract class DefinitionNode extends WorkflowNode {

    protected final WorkflowNodeDefinition definition;
    private final WorkflowNodeExecutorRegistry executorRegistry;

    protected DefinitionNode(WorkflowNodeDefinition definition, WorkflowNodeExecutorRegistry executorRegistry) {
        this.definition = definition;
        this.executorRegistry = executorRegistry;
        this.id = definition.getId();
        this.name = definition.getName();
        this.async = definition.isAsync();
        this.triggerStrategy = definition.getTriggerStrategy();
    }

    public WorkflowNodeDefinition getDefinition() {
        return definition;
    }

    @Override
    protected NodeExecutionResult execute(Workflow workflow) {
        return executorRegistry.get(definition.getType()).execute(definition, workflow.getExecutionContext());
    }
}

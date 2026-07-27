package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.workflow.engine.WorkflowConditionEvaluator;
import com.chatpass.platform.workflow.engine.WorkflowEdgeDefinition;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import com.chatpass.platform.workflow.engine.node.WorkflowNodeExecutorRegistry;
import org.springframework.stereotype.Component;

@Component
public class WorkflowParser {

    private final WorkflowNodeExecutorRegistry executorRegistry;
    private final WorkflowConditionEvaluator conditionEvaluator;

    public WorkflowParser(WorkflowNodeExecutorRegistry executorRegistry, WorkflowConditionEvaluator conditionEvaluator) {
        this.executorRegistry = executorRegistry;
        this.conditionEvaluator = conditionEvaluator;
    }

    public Workflow parse(WorkflowExecutionContext context) {
        Workflow workflow = new Workflow(context, conditionEvaluator);
        for (WorkflowNodeDefinition nodeDefinition : context.getDefinition().getNodes()) {
            workflow.addNode(node(nodeDefinition));
        }
        for (WorkflowEdgeDefinition edgeDefinition : context.getDefinition().getEdges()) {
            workflow.addEdge(edge(edgeDefinition));
        }
        return workflow;
    }

    private WorkflowNode node(WorkflowNodeDefinition definition) {
        switch (definition.getType()) {
            case START:
                return new StartNode(definition, executorRegistry);
            case END:
                return new EndNode(definition, executorRegistry);
            case ACTION:
                return new ActionNode(definition, executorRegistry);
            case CONDITION:
                return new ConditionNode(definition, executorRegistry);
            case WAIT:
                return new WaitNode(definition, executorRegistry);
            case LOOP:
                return new LoopNode(definition, executorRegistry);
            case HTTP:
                return new HttpNode(definition, executorRegistry);
            default:
                throw new IllegalStateException("Unsupported workflow node type: " + definition.getType());
        }
    }

    private WorkflowEdge edge(WorkflowEdgeDefinition definition) {
        WorkflowEdge edge = new WorkflowEdge();
        edge.setId(definition.getId());
        edge.setSource(definition.getFrom());
        edge.setTarget(definition.getTo());
        edge.setConditions(definition.getConditions());
        return edge;
    }
}

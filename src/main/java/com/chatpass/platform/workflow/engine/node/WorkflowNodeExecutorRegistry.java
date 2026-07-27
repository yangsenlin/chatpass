package com.chatpass.platform.workflow.engine.node;

import com.chatpass.platform.workflow.engine.NodeType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowNodeExecutorRegistry {

    private final Map<NodeType, WorkflowNodeExecutor> executors = new EnumMap<>(NodeType.class);

    public WorkflowNodeExecutorRegistry(List<WorkflowNodeExecutor> nodeExecutors) {
        nodeExecutors.forEach(executor -> executors.put(executor.type(), executor));
    }

    public WorkflowNodeExecutor get(NodeType type) {
        WorkflowNodeExecutor executor = executors.get(type);
        if (executor == null) {
            throw new IllegalStateException("No workflow node executor registered for type: " + type);
        }
        return executor;
    }
}

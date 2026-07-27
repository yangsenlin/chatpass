package com.chatpass.platform.workflow.engine;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryWorkflowExecutionStore implements WorkflowExecutionStore {

    private final Map<String, WorkflowExecutionContext> contexts = new ConcurrentHashMap<>();

    @Override
    public void save(WorkflowExecutionContext context) {
        contexts.put(context.getExecutionId(), context);
    }

    @Override
    public Optional<WorkflowExecutionContext> findById(String executionId) {
        return Optional.ofNullable(contexts.get(executionId));
    }
}

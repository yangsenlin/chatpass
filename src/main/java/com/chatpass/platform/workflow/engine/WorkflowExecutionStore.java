package com.chatpass.platform.workflow.engine;

import java.util.Optional;

public interface WorkflowExecutionStore {

    void save(WorkflowExecutionContext context);

    Optional<WorkflowExecutionContext> findById(String executionId);
}

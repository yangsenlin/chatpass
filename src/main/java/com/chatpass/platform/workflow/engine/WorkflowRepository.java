package com.chatpass.platform.workflow.engine;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository {

    List<WorkflowDefinition> findAll();

    Optional<WorkflowDefinition> findById(String id);

    WorkflowDefinition save(WorkflowDefinition definition);

    void deleteById(String id);
}

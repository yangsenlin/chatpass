package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.workflow.mojarvis.ExecutionContextWorkflowListener;
import com.chatpass.platform.workflow.mojarvis.Workflow;
import com.chatpass.platform.workflow.mojarvis.WorkflowParser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class WorkflowEngine {

    private final WorkflowExecutionStore executionStore;
    private final WorkflowValidator workflowValidator;
    private final WorkflowParser workflowParser;
    private final ExecutionContextWorkflowListener executionContextListener;

    public WorkflowEngine(
        WorkflowExecutionStore executionStore,
        WorkflowValidator workflowValidator,
        WorkflowParser workflowParser,
        ExecutionContextWorkflowListener executionContextListener
    ) {
        this.executionStore = executionStore;
        this.workflowValidator = workflowValidator;
        this.workflowParser = workflowParser;
        this.executionContextListener = executionContextListener;
    }

    public WorkflowExecutionResult execute(WorkflowDefinition definition, WorkflowExecutionRequest request) {
        workflowValidator.validate(definition);
        WorkflowExecutionContext context = new WorkflowExecutionContext(definition, request.getMessage(), request.getVariables());
        try {
            context.setStatus(WorkflowStatus.RUNNING);
            Workflow workflow = workflowParser.parse(context);
            workflow.addEventListener(executionContextListener);
            workflow.run();
        } catch (RuntimeException ex) {
            context.setStatus(WorkflowStatus.FAILED);
            context.setErrorMessage(ex.getMessage());
        }
        executionStore.save(context);
        return toResult(context);
    }

    public WorkflowExecutionResult resume(String executionId, Map<String, Object> variables) {
        WorkflowExecutionContext context = executionStore.findById(executionId)
            .orElseThrow(() -> new IllegalArgumentException("Workflow execution not found: " + executionId));
        if (context.getStatus() != WorkflowStatus.SUSPENDED) {
            throw new IllegalStateException("Workflow execution is not suspended: " + executionId);
        }
        context.variables().putAll(variables);
        context.setStatus(WorkflowStatus.RUNNING);
        Workflow workflow = workflowParser.parse(context);
        workflow.addEventListener(executionContextListener);
        workflow.resume(context.getSuspendedNodeId());
        executionStore.save(context);
        return toResult(context);
    }

    private WorkflowExecutionResult toResult(WorkflowExecutionContext context) {
        WorkflowExecutionResult result = new WorkflowExecutionResult();
        result.setExecutionId(context.getExecutionId());
        result.setWorkflowId(context.getDefinition().getId());
        result.setStatus(context.getStatus());
        result.setSuspendedNodeId(context.getSuspendedNodeId());
        result.setVariables(context.variables());
        result.setErrorMessage(context.getErrorMessage());
        result.setStartedAt(context.getStartedAt());
        result.setFinishedAt(Instant.now());
        return result;
    }
}

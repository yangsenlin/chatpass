package com.chatpass.platform.api;

import com.chatpass.platform.workflow.engine.WorkflowDefinition;
import com.chatpass.platform.workflow.engine.WorkflowEngine;
import com.chatpass.platform.workflow.engine.WorkflowExecutionRequest;
import com.chatpass.platform.workflow.engine.WorkflowExecutionResult;
import com.chatpass.platform.workflow.engine.WorkflowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowRepository workflowRepository;
    private final WorkflowEngine workflowEngine;

    public WorkflowController(WorkflowRepository workflowRepository, WorkflowEngine workflowEngine) {
        this.workflowRepository = workflowRepository;
        this.workflowEngine = workflowEngine;
    }

    @GetMapping
    public List<WorkflowDefinition> list() {
        return workflowRepository.findAll();
    }

    @GetMapping("/{id}")
    public WorkflowDefinition get(@PathVariable String id) {
        return workflowRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + id));
    }

    @PostMapping
    public WorkflowDefinition save(@RequestBody WorkflowDefinition definition) {
        return workflowRepository.save(definition);
    }

    @PostMapping("/{id}/execute")
    public WorkflowExecutionResult execute(@PathVariable String id, @RequestBody WorkflowExecutionRequest request) {
        WorkflowDefinition definition = workflowRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + id));
        request.setWorkflowId(id);
        return workflowEngine.execute(definition, request);
    }

    @PostMapping("/executions/{executionId}/resume")
    public WorkflowExecutionResult resume(
        @PathVariable String executionId,
        @RequestBody(required = false) Map<String, Object> variables
    ) {
        return workflowEngine.resume(executionId, variables == null ? Map.of() : variables);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        workflowRepository.deleteById(id);
    }
}

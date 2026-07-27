package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.workflow.WorkflowType;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryWorkflowRepository implements WorkflowRepository {

    private final Map<String, WorkflowDefinition> workflows = new ConcurrentHashMap<>();
    private final WorkflowValidator workflowValidator;

    public InMemoryWorkflowRepository(WorkflowValidator workflowValidator) {
        this.workflowValidator = workflowValidator;
        save(defaultWorkflow());
    }

    @Override
    public List<WorkflowDefinition> findAll() {
        return List.copyOf(workflows.values());
    }

    @Override
    public Optional<WorkflowDefinition> findById(String id) {
        return Optional.ofNullable(workflows.get(id));
    }

    @Override
    public WorkflowDefinition save(WorkflowDefinition definition) {
        if (definition.getId() == null || definition.getId().isBlank()) {
            throw new IllegalArgumentException("Workflow id is required");
        }
        workflowValidator.validate(definition);
        workflows.put(definition.getId(), definition);
        return definition;
    }

    @Override
    public void deleteById(String id) {
        workflows.remove(id);
    }

    private WorkflowDefinition defaultWorkflow() {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("default-auto-reply");
        definition.setName("Default auto reply workflow");

        WorkflowNodeDefinition start = new WorkflowNodeDefinition();
        start.setId("start");
        start.setName("Start");
        start.setType(NodeType.START);

        WorkflowNodeDefinition reply = new WorkflowNodeDefinition();
        reply.setId("reply");
        reply.setName("Auto reply");
        reply.setType(NodeType.ACTION);
        reply.setActionType(WorkflowType.AUTO_REPLY);
        reply.setParameters(new LinkedHashMap<>(Map.of("text", "Message accepted by ChatPass workflow.")));

        WorkflowNodeDefinition end = new WorkflowNodeDefinition();
        end.setId("end");
        end.setName("End");
        end.setType(NodeType.END);

        WorkflowEdgeDefinition startToReply = new WorkflowEdgeDefinition();
        startToReply.setId("start-reply");
        startToReply.setFrom("start");
        startToReply.setTo("reply");

        WorkflowEdgeDefinition replyToEnd = new WorkflowEdgeDefinition();
        replyToEnd.setId("reply-end");
        replyToEnd.setFrom("reply");
        replyToEnd.setTo("end");

        definition.setNodes(List.of(start, reply, end));
        definition.setEdges(List.of(startToReply, replyToEnd));
        return definition;
    }
}

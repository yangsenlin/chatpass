package com.chatpass.platform.workflow.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WorkflowValidator {

    public void validate(WorkflowDefinition definition) {
        if (definition.getNodes().isEmpty()) {
            throw new WorkflowValidationException("Workflow must contain at least one node");
        }
        Map<String, WorkflowNodeDefinition> nodes = uniqueNodes(definition);
        validateEdges(definition, nodes);
        validateStartNodes(definition);
        validateAcyclic(definition, nodes);
    }

    private Map<String, WorkflowNodeDefinition> uniqueNodes(WorkflowDefinition definition) {
        Set<String> seen = new HashSet<>();
        for (WorkflowNodeDefinition node : definition.getNodes()) {
            if (node.getId() == null || node.getId().isBlank()) {
                throw new WorkflowValidationException("Workflow node id is required");
            }
            if (!seen.add(node.getId())) {
                throw new WorkflowValidationException("Duplicate workflow node id: " + node.getId());
            }
        }
        return definition.getNodes().stream().collect(Collectors.toMap(WorkflowNodeDefinition::getId, Function.identity()));
    }

    private void validateEdges(WorkflowDefinition definition, Map<String, WorkflowNodeDefinition> nodes) {
        for (WorkflowEdgeDefinition edge : definition.getEdges()) {
            if (!nodes.containsKey(edge.getFrom())) {
                throw new WorkflowValidationException("Workflow edge source not found: " + edge.getFrom());
            }
            if (!nodes.containsKey(edge.getTo())) {
                throw new WorkflowValidationException("Workflow edge target not found: " + edge.getTo());
            }
        }
    }

    private void validateStartNodes(WorkflowDefinition definition) {
        boolean hasStart = definition.getNodes().stream().anyMatch(node -> node.getType() == NodeType.START);
        if (!hasStart) {
            Set<String> targets = definition.getEdges().stream().map(WorkflowEdgeDefinition::getTo).collect(Collectors.toSet());
            hasStart = definition.getNodes().stream().anyMatch(node -> !targets.contains(node.getId()));
        }
        if (!hasStart) {
            throw new WorkflowValidationException("Workflow must have a START node or a node without incoming edges");
        }
    }

    private void validateAcyclic(WorkflowDefinition definition, Map<String, WorkflowNodeDefinition> nodes) {
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, List<WorkflowEdgeDefinition>> outgoing = definition.getEdges().stream()
            .collect(Collectors.groupingBy(WorkflowEdgeDefinition::getFrom));
        nodes.keySet().forEach(nodeId -> indegree.put(nodeId, 0));
        definition.getEdges().forEach(edge -> indegree.compute(edge.getTo(), (key, value) -> value == null ? 1 : value + 1));

        Queue<String> queue = new ArrayDeque<>();
        indegree.forEach((nodeId, degree) -> {
            if (degree == 0) {
                queue.offer(nodeId);
            }
        });

        int visited = 0;
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            visited++;
            for (WorkflowEdgeDefinition edge : outgoing.getOrDefault(nodeId, List.of())) {
                int degree = indegree.compute(edge.getTo(), (key, value) -> value == null ? 0 : value - 1);
                if (degree == 0) {
                    queue.offer(edge.getTo());
                }
            }
        }

        if (visited != nodes.size()) {
            throw new WorkflowValidationException("Workflow graph must be acyclic");
        }
    }
}

package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.workflow.engine.node.WorkflowNodeExecutorRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkflowEngine {

    private final WorkflowNodeExecutorRegistry nodeExecutorRegistry;
    private final WorkflowConditionEvaluator conditionEvaluator;
    private final WorkflowExecutionStore executionStore;
    private final WorkflowValidator workflowValidator;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public WorkflowEngine(
        WorkflowNodeExecutorRegistry nodeExecutorRegistry,
        WorkflowConditionEvaluator conditionEvaluator,
        WorkflowExecutionStore executionStore,
        WorkflowValidator workflowValidator
    ) {
        this.nodeExecutorRegistry = nodeExecutorRegistry;
        this.conditionEvaluator = conditionEvaluator;
        this.executionStore = executionStore;
        this.workflowValidator = workflowValidator;
    }

    public WorkflowExecutionResult execute(WorkflowDefinition definition, WorkflowExecutionRequest request) {
        workflowValidator.validate(definition);
        WorkflowExecutionContext context = new WorkflowExecutionContext(definition, request.getMessage(), request.getVariables());
        try {
            context.setStatus(WorkflowStatus.RUNNING);
            WorkflowGraph graph = WorkflowGraph.from(context.getDefinition());
            executeQueue(context, graph, new ArrayDeque<>(graph.startNodes()));
            if (context.getStatus() == WorkflowStatus.RUNNING) {
                context.setStatus(WorkflowStatus.FINISHED);
            }
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
        WorkflowGraph graph = WorkflowGraph.from(context.getDefinition());
        Queue<WorkflowNodeDefinition> queue = new ArrayDeque<>();
        for (WorkflowEdgeDefinition edge : graph.outgoing(context.getSuspendedNodeId())) {
            context.recordTrigger(edge.getTo(), edge.getId());
            graph.node(edge.getTo()).ifPresent(queue::offer);
        }
        context.setSuspendedNodeId(null);
        executeQueue(context, graph, queue);
        if (context.getStatus() == WorkflowStatus.RUNNING) {
            context.setStatus(WorkflowStatus.FINISHED);
        }
        executionStore.save(context);
        return toResult(context);
    }

    private void executeQueue(WorkflowExecutionContext context, WorkflowGraph graph, Queue<WorkflowNodeDefinition> queue) {
        Set<String> executedNodes = new HashSet<>();

        while (!queue.isEmpty() && context.getStatus() == WorkflowStatus.RUNNING) {
            WorkflowNodeDefinition node = queue.poll();
            if (!shouldExecute(node, graph, context, executedNodes)) {
                continue;
            }
            executedNodes.add(node.getId());

            NodeExecutionResult result = executeNode(node, context);
            context.putNodeOutputs(node.getId(), result.getOutputs());
            if (result.isSuspended()) {
                context.setStatus(WorkflowStatus.SUSPENDED);
                context.setSuspendedNodeId(node.getId());
                return;
            }

            for (WorkflowEdgeDefinition edge : graph.outgoing(node.getId())) {
                if (conditionEvaluator.matches(edge.getConditions(), context)) {
                    context.recordTrigger(edge.getTo(), edge.getId());
                    graph.node(edge.getTo()).ifPresent(queue::offer);
                }
            }
        }
    }

    private boolean shouldExecute(
        WorkflowNodeDefinition node,
        WorkflowGraph graph,
        WorkflowExecutionContext context,
        Set<String> executedNodes
    ) {
        if (node.getTriggerStrategy() == NodeTriggerStrategy.FIRST && executedNodes.contains(node.getId())) {
            return false;
        }
        int incomingCount = graph.incoming(node.getId()).size();
        if (incomingCount == 0) {
            return !executedNodes.contains(node.getId());
        }
        NodeTriggerStrategy strategy = node.getTriggerStrategy() == null ? NodeTriggerStrategy.ANY : node.getTriggerStrategy();
        switch (strategy) {
            case ANY:
            case FIRST:
                return true;
            case ALL:
                return context.triggeredCount(node.getId()) >= incomingCount;
            default:
                throw new IllegalStateException("Unsupported node trigger strategy: " + strategy);
        }
    }

    private NodeExecutionResult executeNode(WorkflowNodeDefinition node, WorkflowExecutionContext context) {
        if (!node.isAsync()) {
            return nodeExecutorRegistry.get(node.getType()).execute(node, context);
        }
        return CompletableFuture
            .supplyAsync(() -> nodeExecutorRegistry.get(node.getType()).execute(node, context), executorService)
            .join();
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

    private static class WorkflowGraph {

        private final Map<String, WorkflowNodeDefinition> nodes;
        private final Map<String, List<WorkflowEdgeDefinition>> outgoing;
        private final Map<String, List<WorkflowEdgeDefinition>> incoming;

        private WorkflowGraph(
            Map<String, WorkflowNodeDefinition> nodes,
            Map<String, List<WorkflowEdgeDefinition>> outgoing,
            Map<String, List<WorkflowEdgeDefinition>> incoming
        ) {
            this.nodes = nodes;
            this.outgoing = outgoing;
            this.incoming = incoming;
        }

        static WorkflowGraph from(WorkflowDefinition definition) {
            Map<String, WorkflowNodeDefinition> nodes = definition.getNodes().stream()
                .collect(Collectors.toMap(WorkflowNodeDefinition::getId, Function.identity()));
            Map<String, List<WorkflowEdgeDefinition>> outgoing = definition.getEdges().stream()
                .collect(Collectors.groupingBy(WorkflowEdgeDefinition::getFrom));
            Map<String, List<WorkflowEdgeDefinition>> incoming = definition.getEdges().stream()
                .collect(Collectors.groupingBy(WorkflowEdgeDefinition::getTo));
            return new WorkflowGraph(nodes, outgoing, incoming);
        }

        List<WorkflowNodeDefinition> startNodes() {
            List<WorkflowNodeDefinition> starts = nodes.values().stream()
                .filter(node -> node.getType() == NodeType.START)
                .toList();
            if (!starts.isEmpty()) {
                return starts;
            }
            return nodes.values().stream()
                .filter(node -> incoming(node.getId()).isEmpty())
                .toList();
        }

        Optional<WorkflowNodeDefinition> node(String id) {
            return Optional.ofNullable(nodes.get(id));
        }

        List<WorkflowEdgeDefinition> outgoing(String nodeId) {
            return outgoing.getOrDefault(nodeId, new ArrayList<>());
        }

        List<WorkflowEdgeDefinition> incoming(String nodeId) {
            return incoming.getOrDefault(nodeId, new ArrayList<>());
        }
    }
}

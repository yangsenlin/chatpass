package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.NodeTriggerStrategy;
import com.chatpass.platform.workflow.engine.WorkflowConditionEvaluator;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Workflow extends WorkflowNode {

    private final List<WorkflowNode> nodes = new ArrayList<>();
    private final List<WorkflowEdge> edges = new ArrayList<>();
    private final Map<String, WorkflowNode> nodeIndex = new HashMap<>();
    private final Map<String, NodeContext> nodeContexts = new HashMap<>();
    private final Map<String, WorkflowNode> suspendNodes = new HashMap<>();
    private final List<WorkFlowEventListener> eventListeners = new ArrayList<>();
    private final WorkflowExecutionContext executionContext;
    private final WorkflowConditionEvaluator conditionEvaluator;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public Workflow(WorkflowExecutionContext executionContext, WorkflowConditionEvaluator conditionEvaluator) {
        this.executionContext = executionContext;
        this.conditionEvaluator = conditionEvaluator;
        this.id = executionContext.getDefinition().getId();
        this.name = executionContext.getDefinition().getName();
    }

    public WorkflowExecutionContext getExecutionContext() {
        return executionContext;
    }

    public List<WorkflowNode> getNodes() {
        return nodes;
    }

    public List<WorkflowEdge> getEdges() {
        return edges;
    }

    public Map<String, NodeContext> getNodeContexts() {
        return nodeContexts;
    }

    public Map<String, WorkflowNode> getSuspendNodes() {
        return suspendNodes;
    }

    public void addNode(WorkflowNode node) {
        nodes.add(node);
        nodeIndex.put(node.getId(), node);
        nodeContexts.put(node.getId(), new NodeContext(node.getId()));
    }

    public void addEdge(WorkflowEdge edge) {
        edges.add(edge);
        WorkflowNode source = nodeIndex.get(edge.getSource());
        WorkflowNode target = nodeIndex.get(edge.getTarget());
        if (source != null) {
            source.getOutwardEdges().add(edge);
        }
        if (target != null) {
            target.getInwardEdges().add(edge);
        }
    }

    public void addEventListener(WorkFlowEventListener listener) {
        eventListeners.add(listener);
    }

    public void run() {
        notifyEvent(WorkflowEventType.WORKFLOW_START, null, null);
        executeQueue(new ArrayDeque<>(startNodes()));
        if (executionContext.getStatus() == WorkflowStatus.RUNNING) {
            executionContext.setStatus(WorkflowStatus.FINISHED);
            notifyEvent(WorkflowEventType.WORKFLOW_END, null, null);
        }
    }

    public void resume(String suspendedNodeId) {
        notifyEvent(WorkflowEventType.WORKFLOW_RESUME, suspendedNodeId, null);
        Queue<WorkflowNode> queue = new ArrayDeque<>();
        WorkflowNode suspendedNode = nodeIndex.get(suspendedNodeId);
        if (suspendedNode != null) {
            for (WorkflowEdge edge : suspendedNode.getOutwardEdges()) {
                executionContext.recordTrigger(edge.getTarget(), edge.getId());
                WorkflowNode target = nodeIndex.get(edge.getTarget());
                if (target != null) {
                    queue.offer(target);
                }
            }
        }
        executionContext.setSuspendedNodeId(null);
        executeQueue(queue);
        if (executionContext.getStatus() == WorkflowStatus.RUNNING) {
            executionContext.setStatus(WorkflowStatus.FINISHED);
            notifyEvent(WorkflowEventType.WORKFLOW_END, null, null);
        }
    }

    @Override
    protected NodeExecutionResult execute(Workflow workflow) {
        run();
        return NodeExecutionResult.completed(executionContext.variables());
    }

    private void executeQueue(Queue<WorkflowNode> queue) {
        Set<String> executedNodes = new HashSet<>();
        while (!queue.isEmpty() && executionContext.getStatus() == WorkflowStatus.RUNNING) {
            WorkflowNode node = queue.poll();
            if (!shouldExecute(node, executedNodes)) {
                continue;
            }
            executedNodes.add(node.getId());
            NodeExecutionResult result = executeNode(node);
            if (result.isSuspended()) {
                suspend(node);
                return;
            }
            completeNode(node, result);
            for (WorkflowEdge edge : node.getOutwardEdges()) {
                if (conditionEvaluator.matches(edge.getConditions(), executionContext)) {
                    executionContext.recordTrigger(edge.getTarget(), edge.getId());
                    WorkflowNode target = nodeIndex.get(edge.getTarget());
                    if (target != null) {
                        queue.offer(target);
                    }
                }
            }
        }
    }

    private NodeExecutionResult executeNode(WorkflowNode node) {
        try {
            node.setNodeStatus(WorkflowNodeStatus.RUNNING);
            nodeContexts.get(node.getId()).setStatus(WorkflowNodeStatus.RUNNING);
            notifyEvent(WorkflowEventType.NODE_START, node.getId(), null);
            if (!node.isAsync()) {
                return node.execute(this);
            }
            return CompletableFuture.supplyAsync(() -> node.execute(this), executorService).join();
        } catch (RuntimeException ex) {
            node.setNodeStatus(WorkflowNodeStatus.ERROR);
            nodeContexts.get(node.getId()).setStatus(WorkflowNodeStatus.ERROR);
            notifyEvent(WorkflowEventType.NODE_ERROR, node.getId(), ex.getMessage());
            throw ex;
        }
    }

    private void completeNode(WorkflowNode node, NodeExecutionResult result) {
        executionContext.putNodeOutputs(node.getId(), result.getOutputs());
        NodeContext nodeContext = nodeContexts.get(node.getId());
        nodeContext.setOutputs(result.getOutputs());
        nodeContext.setStatus(WorkflowNodeStatus.FINISHED_NORMAL);
        node.setNodeStatus(WorkflowNodeStatus.FINISHED_NORMAL);
        notifyEvent(WorkflowEventType.NODE_END, node.getId(), null);
    }

    private void suspend(WorkflowNode node) {
        executionContext.setStatus(WorkflowStatus.SUSPENDED);
        executionContext.setSuspendedNodeId(node.getId());
        suspendNodes.put(node.getId(), node);
        node.setNodeStatus(WorkflowNodeStatus.SUSPENDED);
        nodeContexts.get(node.getId()).setStatus(WorkflowNodeStatus.SUSPENDED);
        notifyEvent(WorkflowEventType.NODE_SUSPEND, node.getId(), null);
    }

    private boolean shouldExecute(WorkflowNode node, Set<String> executedNodes) {
        if (node.getTriggerStrategy() == NodeTriggerStrategy.FIRST && executedNodes.contains(node.getId())) {
            return false;
        }
        int incomingCount = node.getInwardEdges().size();
        if (incomingCount == 0) {
            return !executedNodes.contains(node.getId());
        }
        NodeTriggerStrategy strategy = node.getTriggerStrategy() == null ? NodeTriggerStrategy.ANY : node.getTriggerStrategy();
        switch (strategy) {
            case ANY:
            case FIRST:
                return true;
            case ALL:
                return executionContext.triggeredCount(node.getId()) >= incomingCount;
            default:
                throw new IllegalStateException("Unsupported node trigger strategy: " + strategy);
        }
    }

    private List<WorkflowNode> startNodes() {
        List<WorkflowNode> starts = nodes.stream()
            .filter(node -> node instanceof StartNode)
            .toList();
        if (!starts.isEmpty()) {
            return starts;
        }
        return nodes.stream()
            .filter(node -> node.getInwardEdges().isEmpty())
            .toList();
    }

    private void notifyEvent(WorkflowEventType type, String nodeId, String message) {
        WorkflowEvent event = new WorkflowEvent(type, id, nodeId, message);
        eventListeners.forEach(listener -> listener.onEvent(event, this));
    }
}

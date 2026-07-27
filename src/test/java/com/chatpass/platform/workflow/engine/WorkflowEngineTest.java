package com.chatpass.platform.workflow.engine;

import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.workflow.WorkflowType;
import com.chatpass.platform.workflow.mojarvis.ExecutionContextWorkflowListener;
import com.chatpass.platform.workflow.mojarvis.WorkflowParser;
import com.chatpass.platform.workflow.engine.node.ActionNodeExecutor;
import com.chatpass.platform.workflow.engine.node.ConditionNodeExecutor;
import com.chatpass.platform.workflow.engine.node.EndNodeExecutor;
import com.chatpass.platform.workflow.engine.node.StartNodeExecutor;
import com.chatpass.platform.workflow.engine.node.WaitNodeExecutor;
import com.chatpass.platform.workflow.engine.node.WorkflowNodeExecutorRegistry;
import com.chatpass.platform.workflow.ActionRegistry;
import com.chatpass.platform.workflow.AutoReplyAction;
import com.chatpass.platform.workflow.NoopAction;
import com.chatpass.platform.workflow.TransferToAgentAction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowEngineTest {

    @Test
    void shouldExecuteDagActionNode() {
        WorkflowEngine engine = engine();
        WorkflowDefinition definition = workflow();

        WorkflowExecutionRequest request = new WorkflowExecutionRequest();
        request.setMessage(message());

        WorkflowExecutionResult result = engine.execute(definition, request);

        assertThat(result.getStatus()).isEqualTo(WorkflowStatus.FINISHED);
        assertThat(result.getVariables()).containsEntry("reply.outputText", "workflow ok");
        assertThat(result.getVariables()).containsEntry("end.ended", true);
        assertThat(result.getVariables()).containsKey(ExecutionContextWorkflowListener.EVENTS_VARIABLE);
    }

    @Test
    void shouldResumeSuspendedWorkflow() {
        WorkflowEngine engine = engine();
        WorkflowDefinition definition = waitWorkflow();

        WorkflowExecutionRequest request = new WorkflowExecutionRequest();
        request.setMessage(message());
        WorkflowExecutionResult suspended = engine.execute(definition, request);

        WorkflowExecutionResult resumed = engine.resume(suspended.getExecutionId(), Map.of("approved", true));

        assertThat(suspended.getStatus()).isEqualTo(WorkflowStatus.SUSPENDED);
        assertThat(resumed.getStatus()).isEqualTo(WorkflowStatus.FINISHED);
        assertThat(resumed.getVariables()).containsEntry("approved", true);
        assertThat(resumed.getVariables()).containsEntry("end.ended", true);
        assertThat(resumed.getVariables()).containsKey(ExecutionContextWorkflowListener.EVENTS_VARIABLE);
    }

    private WorkflowEngine engine() {
        ActionRegistry actionRegistry = new ActionRegistry(List.of(
            new AutoReplyAction(),
            new TransferToAgentAction(),
            new NoopAction()
        ));
        WorkflowNodeExecutorRegistry registry = new WorkflowNodeExecutorRegistry(List.of(
            new StartNodeExecutor(),
            new ActionNodeExecutor(actionRegistry),
            new ConditionNodeExecutor(),
            new WaitNodeExecutor(),
            new EndNodeExecutor()
        ));
        WorkflowConditionEvaluator conditionEvaluator = new WorkflowConditionEvaluator();
        return new WorkflowEngine(
            new InMemoryWorkflowExecutionStore(),
            new WorkflowValidator(),
            new WorkflowParser(registry, conditionEvaluator),
            new ExecutionContextWorkflowListener()
        );
    }

    private WorkflowDefinition workflow() {
        WorkflowNodeDefinition start = node("start", NodeType.START);
        WorkflowNodeDefinition reply = node("reply", NodeType.ACTION);
        reply.setActionType(WorkflowType.AUTO_REPLY);
        reply.setParameters(Map.of("text", "workflow ok"));
        WorkflowNodeDefinition end = node("end", NodeType.END);

        WorkflowEdgeDefinition e1 = edge("e1", "start", "reply");
        WorkflowEdgeDefinition e2 = edge("e2", "reply", "end");

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("test");
        definition.setNodes(List.of(start, reply, end));
        definition.setEdges(List.of(e1, e2));
        return definition;
    }

    private WorkflowDefinition waitWorkflow() {
        WorkflowNodeDefinition start = node("start", NodeType.START);
        WorkflowNodeDefinition wait = node("wait", NodeType.WAIT);
        WorkflowNodeDefinition end = node("end", NodeType.END);

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("wait-test");
        definition.setNodes(List.of(start, wait, end));
        definition.setEdges(List.of(
            edge("e1", "start", "wait"),
            edge("e2", "wait", "end")
        ));
        return definition;
    }

    private WorkflowNodeDefinition node(String id, NodeType type) {
        WorkflowNodeDefinition node = new WorkflowNodeDefinition();
        node.setId(id);
        node.setName(id);
        node.setType(type);
        return node;
    }

    private WorkflowEdgeDefinition edge(String id, String from, String to) {
        WorkflowEdgeDefinition edge = new WorkflowEdgeDefinition();
        edge.setId(id);
        edge.setFrom(from);
        edge.setTo(to);
        return edge;
    }

    private UnifiedMessage message() {
        UnifiedMessage message = new UnifiedMessage();
        message.setMessageId("m1");
        message.setChannel(ChannelType.API);
        message.setSenderId("u1");
        message.setText("hello");
        return message;
    }
}

package com.chatpass.platform.workflow.mojarvis;

import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.workflow.ActionRegistry;
import com.chatpass.platform.workflow.AutoReplyAction;
import com.chatpass.platform.workflow.NoopAction;
import com.chatpass.platform.workflow.TransferToAgentAction;
import com.chatpass.platform.workflow.WorkflowType;
import com.chatpass.platform.workflow.engine.NodeTriggerStrategy;
import com.chatpass.platform.workflow.engine.NodeType;
import com.chatpass.platform.workflow.engine.WorkflowConditionEvaluator;
import com.chatpass.platform.workflow.engine.WorkflowDefinition;
import com.chatpass.platform.workflow.engine.WorkflowEdgeDefinition;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import com.chatpass.platform.workflow.engine.node.ActionNodeExecutor;
import com.chatpass.platform.workflow.engine.node.ConditionNodeExecutor;
import com.chatpass.platform.workflow.engine.node.EndNodeExecutor;
import com.chatpass.platform.workflow.engine.node.StartNodeExecutor;
import com.chatpass.platform.workflow.engine.node.WaitNodeExecutor;
import com.chatpass.platform.workflow.engine.node.WorkflowNodeExecutorRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoJarvisWorkflowAdapterTest {

    @Test
    void shouldAdaptChatPassDefinitionToMoJarvisWorkflow() {
        WorkflowParser parser = new WorkflowParser(registry(), new WorkflowConditionEvaluator());
        WorkflowDefinition definition = definition();

        Workflow workflow = parser.parse(new WorkflowExecutionContext(definition, message(), null));

        assertThat(workflow.getNodes()).hasSize(3);
        assertThat(workflow.getEdges()).hasSize(2);
        WorkflowNode action = workflow.getNodes().stream()
            .filter(node -> "reply".equals(node.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(action).isInstanceOf(ActionNode.class);
        assertThat(action.getTriggerStrategy()).isEqualTo(NodeTriggerStrategy.ALL);
        assertThat(action.getInwardEdges()).hasSize(1);
        assertThat(action.getOutwardEdges()).hasSize(1);
        assertThat(workflow.getNodes().stream().filter(StartNode.class::isInstance)).hasSize(1);
        assertThat(workflow.getNodes().stream().filter(EndNode.class::isInstance)).hasSize(1);
    }

    private WorkflowNodeExecutorRegistry registry() {
        ActionRegistry actionRegistry = new ActionRegistry(List.of(
            new AutoReplyAction(),
            new TransferToAgentAction(),
            new NoopAction()
        ));
        return new WorkflowNodeExecutorRegistry(List.of(
            new StartNodeExecutor(),
            new ActionNodeExecutor(actionRegistry),
            new ConditionNodeExecutor(),
            new WaitNodeExecutor(),
            new EndNodeExecutor()
        ));
    }

    private WorkflowDefinition definition() {
        WorkflowNodeDefinition start = node("start", NodeType.START);
        WorkflowNodeDefinition reply = node("reply", NodeType.ACTION);
        reply.setActionType(WorkflowType.AUTO_REPLY);
        reply.setTriggerStrategy(NodeTriggerStrategy.ALL);
        WorkflowNodeDefinition end = node("end", NodeType.END);

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId("wf");
        definition.setNodes(List.of(start, reply, end));
        definition.setEdges(List.of(edge("e1", "start", "reply"), edge("e2", "reply", "end")));
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

package com.chatpass.platform.message;

import com.chatpass.platform.output.OutboundMessage;
import com.chatpass.platform.output.OutputChannelAdapter;
import com.chatpass.platform.routing.RouteDecision;
import com.chatpass.platform.workflow.ActionResult;
import com.chatpass.platform.workflow.ActionStatus;
import com.chatpass.platform.workflow.WorkflowExecutor;
import com.chatpass.platform.routing.MessageRouter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageIngressService {

    private final MessageRouter messageRouter;
    private final WorkflowExecutor workflowExecutor;
    private final OutputChannelAdapter outputChannelAdapter;

    public MessageIngressService(
        MessageRouter messageRouter,
        WorkflowExecutor workflowExecutor,
        OutputChannelAdapter outputChannelAdapter
    ) {
        this.messageRouter = messageRouter;
        this.workflowExecutor = workflowExecutor;
        this.outputChannelAdapter = outputChannelAdapter;
    }

    public MessageProcessingResult receive(UnifiedMessage message) {
        RouteDecision decision = messageRouter.route(message);
        ActionResult actionResult = workflowExecutor.execute(message, decision);
        OutboundMessage outboundMessage = buildOutboundMessage(message, actionResult);
        if (actionResult.getStatus() == ActionStatus.SUCCESS && outboundMessage.getText() != null) {
            outputChannelAdapter.send(outboundMessage);
        }
        return new MessageProcessingResult(message, decision, actionResult, outboundMessage);
    }

    private OutboundMessage buildOutboundMessage(UnifiedMessage inbound, ActionResult actionResult) {
        OutboundMessage outbound = new OutboundMessage();
        outbound.setMessageId(UUID.randomUUID().toString());
        outbound.setChannel(inbound.getChannel());
        outbound.setTargetUserId(inbound.getSenderId());
        outbound.setConversationId(inbound.getConversationId());
        outbound.setText(actionResult.getOutputText());
        outbound.getAttributes().putAll(inbound.getAttributes());
        outbound.getAttributes().put("sourceMessageId", inbound.getMessageId());
        outbound.getAttributes().put("actionStatus", actionResult.getStatus());
        outbound.getAttributes().putAll(actionResult.getOutputs());
        return outbound;
    }
}

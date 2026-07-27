package com.chatpass.platform.message;

import com.chatpass.platform.history.AsyncHistoryIndexer;
import com.chatpass.platform.output.OutboundMessage;
import com.chatpass.platform.output.OutputChannelAdapter;
import com.chatpass.platform.routing.RouteDecision;
import com.chatpass.platform.workflow.ActionResult;
import com.chatpass.platform.workflow.ActionStatus;
import com.chatpass.platform.workflow.WorkflowExecutor;
import com.chatpass.platform.routing.MessageRouter;
import com.chatpass.platform.reliability.MessageReliabilityStore;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageIngressService {

    private final MessageRouter messageRouter;
    private final WorkflowExecutor workflowExecutor;
    private final OutputChannelAdapter outputChannelAdapter;
    private final MessageReliabilityStore reliabilityStore;
    private final AsyncHistoryIndexer historyIndexer;

    public MessageIngressService(
        MessageRouter messageRouter,
        WorkflowExecutor workflowExecutor,
        OutputChannelAdapter outputChannelAdapter,
        MessageReliabilityStore reliabilityStore,
        AsyncHistoryIndexer historyIndexer
    ) {
        this.messageRouter = messageRouter;
        this.workflowExecutor = workflowExecutor;
        this.outputChannelAdapter = outputChannelAdapter;
        this.reliabilityStore = reliabilityStore;
        this.historyIndexer = historyIndexer;
    }

    public MessageProcessingResult receive(UnifiedMessage message) {
        if (!reliabilityStore.tryAcquire(message)) {
            ActionResult duplicate = ActionResult.skipped("Duplicate message ignored");
            return new MessageProcessingResult(message, null, duplicate, null);
        }
        RouteDecision decision = messageRouter.route(message);
        reliabilityStore.markStatus(message.getMessageId(), "ROUTED", null);
        ActionResult actionResult = workflowExecutor.execute(message, decision);
        reliabilityStore.markStatus(message.getMessageId(), "EXECUTED", actionResult.getErrorMessage());
        OutboundMessage outboundMessage = buildOutboundMessage(message, actionResult);
        if (actionResult.getStatus() == ActionStatus.SUCCESS && outboundMessage.getText() != null) {
            reliabilityStore.recordOutbox(message.getMessageId(), outboundMessage);
            outputChannelAdapter.send(outboundMessage);
            reliabilityStore.markStatus(message.getMessageId(), "DELIVERING", null);
        } else if (actionResult.getStatus() == ActionStatus.FAILED) {
            reliabilityStore.markStatus(message.getMessageId(), "FAILED", actionResult.getErrorMessage());
        }
        MessageProcessingResult result = new MessageProcessingResult(message, decision, actionResult, outboundMessage);
        historyIndexer.index(result);
        return result;
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

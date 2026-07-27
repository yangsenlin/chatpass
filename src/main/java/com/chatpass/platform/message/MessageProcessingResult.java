package com.chatpass.platform.message;

import com.chatpass.platform.output.OutboundMessage;
import com.chatpass.platform.routing.RouteDecision;
import com.chatpass.platform.workflow.ActionResult;

public class MessageProcessingResult {

    private final UnifiedMessage message;
    private final RouteDecision decision;
    private final ActionResult actionResult;
    private final OutboundMessage outboundMessage;

    public MessageProcessingResult(
        UnifiedMessage message,
        RouteDecision decision,
        ActionResult actionResult,
        OutboundMessage outboundMessage
    ) {
        this.message = message;
        this.decision = decision;
        this.actionResult = actionResult;
        this.outboundMessage = outboundMessage;
    }

    public UnifiedMessage getMessage() {
        return message;
    }

    public RouteDecision getDecision() {
        return decision;
    }

    public ActionResult getActionResult() {
        return actionResult;
    }

    public OutboundMessage getOutboundMessage() {
        return outboundMessage;
    }
}

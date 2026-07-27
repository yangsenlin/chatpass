package com.chatpass.platform.workflow;

import org.springframework.stereotype.Component;

@Component
public class AutoReplyAction implements WorkflowAction {

    @Override
    public WorkflowType type() {
        return WorkflowType.AUTO_REPLY;
    }

    @Override
    public ActionResult execute(ActionExecutionRequest request) {
        Object text = request.getRule().getWorkflow().getParameters().get("text");
        String reply = text == null ? "Message accepted by ChatPass." : String.valueOf(text);
        ActionResult result = ActionResult.success(reply);
        result.getOutputs().put("replyText", reply);
        return result;
    }
}

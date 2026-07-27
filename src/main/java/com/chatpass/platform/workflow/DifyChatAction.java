package com.chatpass.platform.workflow;

import io.github.chatpass.dify.api.DifyChatApi;
import io.github.chatpass.dify.data.enums.ResponseMode;
import io.github.chatpass.dify.data.request.ChatMessageRequest;
import io.github.chatpass.dify.data.response.ChatMessageResponse;
import io.github.chatpass.dify.exception.DifyApiException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DifyChatAction implements WorkflowAction {

    private final ObjectProvider<DifyChatApi> difyChatApiProvider;

    public DifyChatAction(ObjectProvider<DifyChatApi> difyChatApiProvider) {
        this.difyChatApiProvider = difyChatApiProvider;
    }

    @Override
    public WorkflowType type() {
        return WorkflowType.DIFY_CHAT;
    }

    @Override
    public ActionResult execute(ActionExecutionRequest request) {
        DifyChatApi difyChatApi = difyChatApiProvider.getIfAvailable();
        if (difyChatApi == null) {
            return ActionResult.failed("Dify is disabled or not configured");
        }

        try {
            ChatMessageResponse response = difyChatApi.sendChatMessage(buildRequest(request));
            ActionResult result = ActionResult.success(response.getAnswer());
            result.getOutputs().put("messageId", response.getMessageId());
            result.getOutputs().put("conversationId", response.getConversationId());
            result.getOutputs().put("answer", response.getAnswer());
            return result;
        } catch (DifyApiException ex) {
            return ActionResult.failed(ex.getMessage());
        }
    }

    private ChatMessageRequest buildRequest(ActionExecutionRequest request) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("channel", request.getMessage().getChannel());
        inputs.put("message_id", request.getMessage().getMessageId());
        inputs.put("receiver_id", request.getMessage().getReceiverId());
        inputs.putAll(request.getMessage().getAttributes());

        return ChatMessageRequest.builder()
            .query(request.getMessage().getText())
            .inputs(inputs)
            .responseMode(ResponseMode.BLOCKING)
            .user(request.getMessage().getSenderId())
            .conversationId(request.getMessage().getConversationId())
            .build();
    }
}

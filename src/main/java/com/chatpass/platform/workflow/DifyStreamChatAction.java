package com.chatpass.platform.workflow;

import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.chatpass.platform.mqtt.control.MqttRouteTable;
import com.chatpass.platform.stream.MqttStreamPublisher;
import com.chatpass.platform.stream.RedisStreamStore;
import com.chatpass.platform.stream.StreamEventType;
import com.chatpass.platform.stream.StreamMessage;
import io.github.chatpass.dify.api.DifyChatApi;
import io.github.chatpass.dify.api.callback.ChatStreamCallback;
import io.github.chatpass.dify.data.enums.ResponseMode;
import io.github.chatpass.dify.data.event.ErrorEvent;
import io.github.chatpass.dify.data.event.MessageEndEvent;
import io.github.chatpass.dify.data.event.MessageEvent;
import io.github.chatpass.dify.data.request.ChatMessageRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DifyStreamChatAction implements WorkflowAction {

    private final ObjectProvider<DifyChatApi> difyChatApiProvider;
    private final RedisStreamStore streamStore;
    private final MqttStreamPublisher streamPublisher;
    private final MqttRouteTable routeTable;
    private final MqttBrokerProperties mqttProperties;

    public DifyStreamChatAction(
        ObjectProvider<DifyChatApi> difyChatApiProvider,
        RedisStreamStore streamStore,
        MqttStreamPublisher streamPublisher,
        MqttRouteTable routeTable,
        MqttBrokerProperties mqttProperties
    ) {
        this.difyChatApiProvider = difyChatApiProvider;
        this.streamStore = streamStore;
        this.streamPublisher = streamPublisher;
        this.routeTable = routeTable;
        this.mqttProperties = mqttProperties;
    }

    @Override
    public WorkflowType type() {
        return WorkflowType.DIFY_STREAM_CHAT;
    }

    @Override
    public ActionResult execute(ActionExecutionRequest request) {
        DifyChatApi difyChatApi = difyChatApiProvider.getIfAvailable();
        if (difyChatApi == null) {
            return ActionResult.failed("Dify is disabled or not configured");
        }

        String streamId = UUID.randomUUID().toString();
        AtomicInteger sequence = new AtomicInteger();
        AtomicBoolean finished = new AtomicBoolean(false);
        StreamMessage start = message(request.getMessage(), streamId, sequence.getAndIncrement(), StreamEventType.START, null);
        streamStore.create(start);
        bindStreamRoute(start);
        streamPublisher.publish(start);

        try {
            difyChatApi.sendChatMessageStream(buildRequest(request), new ChatStreamCallback() {
                @Override
                public void onMessage(MessageEvent event) {
                    if (streamStore.isCancelled(streamId)) {
                        return;
                    }
                    StreamMessage chunk = message(
                        request.getMessage(),
                        streamId,
                        sequence.getAndIncrement(),
                        StreamEventType.CHUNK,
                        event.getAnswer()
                    );
                    streamStore.append(chunk);
                    streamPublisher.publish(chunk);
                }

                @Override
                public void onMessageEnd(MessageEndEvent event) {
                    complete(request.getMessage(), streamId, sequence, finished);
                }

                @Override
                public void onError(ErrorEvent event) {
                    fail(request.getMessage(), streamId, sequence, event.getMessage(), finished);
                }

                @Override
                public void onException(Throwable throwable) {
                    fail(request.getMessage(), streamId, sequence, throwable.getMessage(), finished);
                }

                @Override
                public void onComplete() {
                    complete(request.getMessage(), streamId, sequence, finished);
                }
            });
        } catch (RuntimeException ex) {
            fail(request.getMessage(), streamId, sequence, ex.getMessage(), finished);
            return ActionResult.failed(ex.getMessage());
        }

        ActionResult result = ActionResult.success(streamStore.content(streamId));
        result.getOutputs().put("streamId", streamId);
        result.getOutputs().put("streamState", "COMPLETED");
        result.getOutputs().put("content", streamStore.content(streamId));
        return result;
    }

    private void complete(UnifiedMessage message, String streamId, AtomicInteger sequence, AtomicBoolean finished) {
        if (streamStore.isCancelled(streamId)) {
            finished.set(true);
            return;
        }
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        streamStore.complete(streamId);
        streamPublisher.publish(message(message, streamId, sequence.getAndIncrement(), StreamEventType.DONE, null));
    }

    private void fail(UnifiedMessage message, String streamId, AtomicInteger sequence, String errorMessage, AtomicBoolean finished) {
        if (streamStore.isCancelled(streamId)) {
            finished.set(true);
            return;
        }
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        streamStore.fail(streamId, errorMessage);
        streamPublisher.publish(message(message, streamId, sequence.getAndIncrement(), StreamEventType.ERROR, errorMessage));
    }

    private StreamMessage message(UnifiedMessage source, String streamId, int sequence, StreamEventType type, String content) {
        StreamMessage message = new StreamMessage();
        message.setTenantId(source.getTenantId());
        message.setAppId(source.getAppId());
        message.setConversationId(source.getConversationId());
        message.setMessageId(source.getMessageId());
        message.setStreamId(streamId);
        message.setSequence(sequence);
        message.setType(type);
        message.setContent(content);
        message.getMetadata().put("source", "dify");
        return message;
    }

    private void bindStreamRoute(StreamMessage start) {
        if (start.getConversationId() != null && !start.getConversationId().isBlank()) {
            routeTable.bindConversation(start.getTenantId(), start.getConversationId(), mqttProperties.getClusterId());
        }
        routeTable.bindStream(
            start.getTenantId(),
            start.getConversationId(),
            start.getStreamId(),
            mqttProperties.getClusterId(),
            mqttProperties.getNodeId()
        );
    }

    private ChatMessageRequest buildRequest(ActionExecutionRequest request) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("tenant_id", request.getMessage().getTenantId());
        inputs.put("app_id", request.getMessage().getAppId());
        inputs.put("channel", request.getMessage().getChannel());
        inputs.putAll(request.getMessage().getAttributes());

        return ChatMessageRequest.builder()
            .query(request.getMessage().getText())
            .inputs(inputs)
            .responseMode(ResponseMode.STREAMING)
            .user(request.getMessage().getSenderId())
            .conversationId(request.getMessage().getConversationId())
            .build();
    }
}

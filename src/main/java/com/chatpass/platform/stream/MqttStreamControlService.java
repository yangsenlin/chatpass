package com.chatpass.platform.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class MqttStreamControlService {

    private final RedisStreamStore streamStore;
    private final MqttStreamPublisher streamPublisher;
    private final ObjectMapper objectMapper;

    public MqttStreamControlService(
        RedisStreamStore streamStore,
        MqttStreamPublisher streamPublisher,
        ObjectMapper objectMapper
    ) {
        this.streamStore = streamStore;
        this.streamPublisher = streamPublisher;
        this.objectMapper = objectMapper;
    }

    public boolean handle(String topic, byte[] payload) {
        if (topic == null || !topic.endsWith("/control")) {
            return false;
        }
        StreamControlCommand command = parse(payload);
        if ("CANCEL".equalsIgnoreCase(command.getType())) {
            streamStore.cancel(command.getStreamId());
            StreamMessage cancelled = event(command.getStreamId(), StreamEventType.CANCELLED);
            cancelled.setContent(command.getReason());
            streamPublisher.publish(cancelled);
            return true;
        }
        if ("RESUME".equalsIgnoreCase(command.getType())) {
            streamStore.chunksAfter(command.getStreamId(), command.getLastSequence()).forEach(streamPublisher::publish);
            return true;
        }
        return false;
    }

    private StreamControlCommand parse(byte[] payload) {
        try {
            return objectMapper.readValue(payload, StreamControlCommand.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid stream control command", ex);
        }
    }

    private StreamMessage event(String streamId, StreamEventType type) {
        StreamMessage message = new StreamMessage();
        message.setStreamId(streamId);
        message.setType(type);
        fillRouteFields(streamId, message);
        return message;
    }

    private void fillRouteFields(String streamId, StreamMessage message) {
        var state = streamStore.state(streamId);
        message.setTenantId(stringValue(state.get("tenantId")));
        message.setAppId(stringValue(state.get("appId")));
        message.setConversationId(stringValue(state.get("conversationId")));
        message.setMessageId(stringValue(state.get("messageId")));
    }

    private String stringValue(Object value) {
        String text = value == null ? null : String.valueOf(value);
        return text == null || text.isBlank() ? null : text;
    }
}

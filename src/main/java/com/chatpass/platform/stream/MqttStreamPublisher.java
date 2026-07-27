package com.chatpass.platform.stream;

import com.chatpass.platform.mqtt.MqttBrokerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MqttStreamPublisher {

    private final ObjectProvider<MqttBrokerService> brokerServiceProvider;
    private final ObjectMapper objectMapper;

    public MqttStreamPublisher(ObjectProvider<MqttBrokerService> brokerServiceProvider, ObjectMapper objectMapper) {
        this.brokerServiceProvider = brokerServiceProvider;
        this.objectMapper = objectMapper;
    }

    public void publish(StreamMessage message) {
        MqttBrokerService brokerService = brokerServiceProvider.getIfAvailable();
        if (brokerService == null) {
            return;
        }
        brokerService.publishFromServer(topic(message), toJson(message).getBytes(StandardCharsets.UTF_8), qoS(message), false);
    }

    public String topic(StreamMessage message) {
        return "/chatpass/"
            + safe(message.getTenantId())
            + "/"
            + safe(message.getAppId())
            + "/conversation/"
            + safe(message.getConversationId())
            + "/stream/"
            + safe(message.getStreamId())
            + "/"
            + message.getType().name().toLowerCase();
    }

    private MqttQoS qoS(StreamMessage message) {
        if (message.getType() == StreamEventType.CHUNK || message.getType() == StreamEventType.HEARTBEAT) {
            return MqttQoS.AT_LEAST_ONCE;
        }
        return MqttQoS.AT_LEAST_ONCE;
    }

    private String toJson(StreamMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize stream message", ex);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "default" : value;
    }
}

package com.chatpass.platform.output;

import com.chatpass.platform.mqtt.MqttBrokerService;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MqttOutboundChannelSender {

    private final ObjectProvider<MqttBrokerService> brokerServiceProvider;

    public MqttOutboundChannelSender(ObjectProvider<MqttBrokerService> brokerServiceProvider) {
        this.brokerServiceProvider = brokerServiceProvider;
    }

    public void sendIfMqtt(OutboundMessage message) {
        if (message.getText() == null) {
            return;
        }
        String topic = topic(message);
        if (topic == null || topic.isBlank()) {
            return;
        }
        MqttBrokerService brokerService = brokerServiceProvider.getIfAvailable();
        if (brokerService != null) {
            brokerService.publishLocal(topic, message.getText().getBytes(StandardCharsets.UTF_8), MqttQoS.AT_LEAST_ONCE, false);
        }
    }

    private String topic(OutboundMessage message) {
        Object responseTopic = message.getAttributes().get("responseTopic");
        if (responseTopic != null) {
            return String.valueOf(responseTopic);
        }
        Object mqttReplyTopic = message.getAttributes().get("mqtt.replyTopic");
        if (mqttReplyTopic != null) {
            return String.valueOf(mqttReplyTopic);
        }
        Object mqttTopic = message.getAttributes().get("mqtt.topic");
        return mqttTopic == null ? null : String.valueOf(mqttTopic);
    }
}

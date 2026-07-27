package com.chatpass.platform.mqtt;

import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.MessageIngressService;
import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.mqtt.session.MqttClientSession;
import com.chatpass.platform.stream.MqttStreamControlService;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class MqttIngressBridge {

    private final MqttBrokerProperties properties;
    private final MessageIngressService messageIngressService;
    private final MqttStreamControlService streamControlService;

    public MqttIngressBridge(
        MqttBrokerProperties properties,
        MessageIngressService messageIngressService,
        MqttStreamControlService streamControlService
    ) {
        this.properties = properties;
        this.messageIngressService = messageIngressService;
        this.streamControlService = streamControlService;
    }

    public void bridge(MqttClientSession session, String topic, byte[] payload, MqttQoS qoS, boolean retain) {
        if (!properties.isBridgeIngress()) {
            return;
        }
        if (streamControlService.handle(topic, payload)) {
            return;
        }
        UnifiedMessage message = new UnifiedMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setChannel(ChannelType.MQTT);
        message.setSenderId(session.getClientId());
        message.setReceiverId(topic);
        message.setConversationId(topic);
        message.setText(new String(payload, StandardCharsets.UTF_8));
        message.setTimestamp(Instant.now());
        message.setAttributes(attributes(session, topic, qoS, retain));
        messageIngressService.receive(message);
    }

    private Map<String, Object> attributes(MqttClientSession session, String topic, MqttQoS qoS, boolean retain) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("mqtt.topic", topic);
        attributes.put("mqtt.qos", qoS.value());
        attributes.put("mqtt.retain", retain);
        attributes.put("mqtt.username", session.getUsername());
        return attributes;
    }
}

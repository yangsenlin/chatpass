package com.chatpass.platform.mqtt.offline;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.time.Instant;
import java.util.UUID;

public class MqttOfflineMessage {

    private final String id = UUID.randomUUID().toString();
    private final String topic;
    private final byte[] payload;
    private final MqttQoS qoS;
    private final Instant createdAt = Instant.now();

    public MqttOfflineMessage(String topic, byte[] payload, MqttQoS qoS) {
        this.topic = topic;
        this.payload = payload;
        this.qoS = qoS;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public byte[] getPayload() {
        return payload;
    }

    public MqttQoS getQoS() {
        return qoS;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

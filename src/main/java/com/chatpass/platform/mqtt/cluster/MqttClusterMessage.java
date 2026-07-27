package com.chatpass.platform.mqtt.cluster;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.time.Instant;

public class MqttClusterMessage {

    private final String sourceNodeId;
    private final String topic;
    private final byte[] payload;
    private final MqttQoS qoS;
    private final boolean retain;
    private final Instant createdAt = Instant.now();

    public MqttClusterMessage(String sourceNodeId, String topic, byte[] payload, MqttQoS qoS, boolean retain) {
        this.sourceNodeId = sourceNodeId;
        this.topic = topic;
        this.payload = payload;
        this.qoS = qoS;
        this.retain = retain;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
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

    public boolean isRetain() {
        return retain;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

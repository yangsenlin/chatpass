package com.chatpass.platform.mqtt.cluster;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.time.Instant;

public class MqttClusterMessage {

    private String sourceNodeId;
    private String topic;
    private byte[] payload;
    private MqttQoS qoS;
    private boolean retain;
    private Instant createdAt = Instant.now();

    public MqttClusterMessage() {
    }

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

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public MqttQoS getQoS() {
        return qoS;
    }

    public void setQoS(MqttQoS qoS) {
        this.qoS = qoS;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

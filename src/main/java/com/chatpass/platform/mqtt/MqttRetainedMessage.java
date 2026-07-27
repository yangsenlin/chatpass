package com.chatpass.platform.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;

public class MqttRetainedMessage {

    private final String topic;
    private final byte[] payload;
    private final MqttQoS qoS;

    public MqttRetainedMessage(String topic, byte[] payload, MqttQoS qoS) {
        this.topic = topic;
        this.payload = payload;
        this.qoS = qoS;
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
}

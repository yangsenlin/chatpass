package com.chatpass.platform.mqtt.cluster;

public interface MqttClusterBus {

    void broadcast(MqttClusterMessage message);
}

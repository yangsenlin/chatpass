package com.chatpass.platform.mqtt.offline;

import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MqttOfflineMessageStore {

    private final Map<String, List<MqttOfflineMessage>> messagesByClientId = new ConcurrentHashMap<>();

    public void save(String clientId, String topic, byte[] payload, MqttQoS qoS) {
        messagesByClientId
            .computeIfAbsent(clientId, ignored -> Collections.synchronizedList(new ArrayList<>()))
            .add(new MqttOfflineMessage(topic, payload, qoS));
    }

    public List<MqttOfflineMessage> drain(String clientId) {
        List<MqttOfflineMessage> messages = messagesByClientId.remove(clientId);
        if (messages == null) {
            return List.of();
        }
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }
}

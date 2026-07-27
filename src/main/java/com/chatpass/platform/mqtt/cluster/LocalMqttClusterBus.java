package com.chatpass.platform.mqtt.cluster;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LocalMqttClusterBus implements MqttClusterBus {

    private final MqttBrokerProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    public LocalMqttClusterBus(MqttBrokerProperties properties, ApplicationEventPublisher eventPublisher) {
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void broadcast(MqttClusterMessage message) {
        if (properties.isClusterEnabled()) {
            eventPublisher.publishEvent(message);
        }
    }
}

package com.chatpass.platform.protection;

import com.chatpass.platform.mqtt.session.MqttSessionRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class ChatPassMetrics {

    private final Counter ingressMessages;
    private final Counter webhookMessages;
    private final Counter mqttPublishes;
    private final Counter mqttRejectedConnections;
    private final Counter mqttRejectedMessages;
    private final Counter rateLimitedRequests;
    private final Counter oversizedMessages;

    public ChatPassMetrics(MeterRegistry meterRegistry, ObjectProvider<MqttSessionRegistry> sessionRegistryProvider) {
        this.ingressMessages = Counter.builder("chatpass_ingress_messages_total").register(meterRegistry);
        this.webhookMessages = Counter.builder("chatpass_webhook_messages_total").register(meterRegistry);
        this.mqttPublishes = Counter.builder("chatpass_mqtt_publishes_total").register(meterRegistry);
        this.mqttRejectedConnections = Counter.builder("chatpass_mqtt_rejected_connections_total").register(meterRegistry);
        this.mqttRejectedMessages = Counter.builder("chatpass_mqtt_rejected_messages_total").register(meterRegistry);
        this.rateLimitedRequests = Counter.builder("chatpass_rate_limited_requests_total").register(meterRegistry);
        this.oversizedMessages = Counter.builder("chatpass_oversized_messages_total").register(meterRegistry);
        Gauge.builder("chatpass_mqtt_active_connections", sessionRegistryProvider, provider -> {
            MqttSessionRegistry registry = provider.getIfAvailable();
            return registry == null ? 0 : registry.activeConnectionCount();
        }).register(meterRegistry);
    }

    public void incrementIngressMessages() {
        ingressMessages.increment();
    }

    public void incrementWebhookMessages() {
        webhookMessages.increment();
    }

    public void incrementMqttPublishes() {
        mqttPublishes.increment();
    }

    public void incrementMqttRejectedConnections() {
        mqttRejectedConnections.increment();
    }

    public void incrementMqttRejectedMessages() {
        mqttRejectedMessages.increment();
    }

    public void incrementRateLimitedRequests() {
        rateLimitedRequests.increment();
    }

    public void incrementOversizedMessages() {
        oversizedMessages.increment();
    }
}

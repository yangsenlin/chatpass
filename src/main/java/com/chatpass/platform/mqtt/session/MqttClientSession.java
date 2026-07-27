package com.chatpass.platform.mqtt.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MqttClientSession {

    private final String clientId;
    private final Channel channel;
    private final Map<String, MqttQoS> subscriptions = new ConcurrentHashMap<>();
    private boolean cleanSession = true;
    private String username;
    private Instant connectedAt = Instant.now();

    public MqttClientSession(String clientId, Channel channel) {
        this.clientId = clientId;
        this.channel = channel;
    }

    public String getClientId() {
        return clientId;
    }

    public Channel getChannel() {
        return channel;
    }

    public Map<String, MqttQoS> getSubscriptions() {
        return subscriptions;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }
}

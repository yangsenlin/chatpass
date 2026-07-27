package com.chatpass.platform.mqtt.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MqttSessionRegistry {

    private final Map<String, MqttClientSession> sessionsByClientId = new ConcurrentHashMap<>();
    private final Map<Channel, String> clientIdsByChannel = new ConcurrentHashMap<>();
    private final Map<String, Map<String, MqttQoS>> durableSubscriptionsByClientId = new ConcurrentHashMap<>();

    public MqttClientSession register(String clientId, String username, boolean cleanSession, Channel channel) {
        MqttClientSession session = new MqttClientSession(clientId, channel);
        session.setUsername(username);
        session.setCleanSession(cleanSession);
        if (!cleanSession) {
            session.getSubscriptions().putAll(durableSubscriptionsByClientId.getOrDefault(clientId, Map.of()));
        } else {
            durableSubscriptionsByClientId.remove(clientId);
        }
        sessionsByClientId.put(clientId, session);
        clientIdsByChannel.put(channel, clientId);
        return session;
    }

    public Optional<MqttClientSession> findByChannel(Channel channel) {
        return Optional.ofNullable(clientIdsByChannel.get(channel)).map(sessionsByClientId::get);
    }

    public void unregister(Channel channel) {
        String clientId = clientIdsByChannel.remove(channel);
        if (clientId != null) {
            MqttClientSession session = sessionsByClientId.remove(clientId);
            if (session != null && !session.isCleanSession()) {
                durableSubscriptionsByClientId.put(clientId, new ConcurrentHashMap<>(session.getSubscriptions()));
            } else {
                durableSubscriptionsByClientId.remove(clientId);
            }
        }
    }

    public void subscribe(Channel channel, String topicFilter, MqttQoS qoS) {
        findByChannel(channel).ifPresent(session -> session.getSubscriptions().put(topicFilter, qoS));
    }

    public void unsubscribe(Channel channel, List<String> topicFilters) {
        findByChannel(channel).ifPresent(session -> topicFilters.forEach(session.getSubscriptions()::remove));
    }

    public List<MqttClientSession> subscribers(String topic) {
        return sessionsByClientId.values().stream()
            .filter(session -> session.getSubscriptions().keySet().stream().anyMatch(filter -> TopicMatcher.matches(filter, topic)))
            .toList();
    }

    public Map<String, MqttQoS> offlineSubscribers(String topic) {
        Map<String, MqttQoS> subscribers = new ConcurrentHashMap<>();
        durableSubscriptionsByClientId.forEach((clientId, subscriptions) -> {
            if (!sessionsByClientId.containsKey(clientId)) {
                subscriptions.entrySet().stream()
                    .filter(entry -> TopicMatcher.matches(entry.getKey(), topic))
                    .findFirst()
                    .ifPresent(entry -> subscribers.put(clientId, entry.getValue()));
            }
        });
        return subscribers;
    }
}

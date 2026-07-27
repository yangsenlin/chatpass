package com.chatpass.platform.mqtt.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

    public MqttSessionRegistry(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public MqttClientSession register(String clientId, String username, boolean cleanSession, Channel channel) {
        MqttClientSession session = new MqttClientSession(clientId, channel);
        session.setUsername(username);
        session.setCleanSession(cleanSession);
        if (!cleanSession) {
            durableSubscriptionsByClientId.put(clientId, loadSubscriptions(clientId));
            session.getSubscriptions().putAll(durableSubscriptionsByClientId.getOrDefault(clientId, Map.of()));
        } else {
            durableSubscriptionsByClientId.remove(clientId);
            redisTemplate.delete(subscriptionKey(clientId));
        }
        sessionsByClientId.put(clientId, session);
        clientIdsByChannel.put(channel, clientId);
        return session;
    }

    public Optional<MqttClientSession> findByChannel(Channel channel) {
        return Optional.ofNullable(clientIdsByChannel.get(channel)).map(sessionsByClientId::get);
    }

    public int activeConnectionCount() {
        return sessionsByClientId.size();
    }

    public int activeConnectionCountByUsername(String username) {
        return (int) sessionsByClientId.values().stream()
            .filter(session -> username.equals(usernameKey(session.getUsername())))
            .count();
    }

    private String usernameKey(String username) {
        return username == null || username.isBlank() ? "anonymous" : username;
    }

    public void unregister(Channel channel) {
        String clientId = clientIdsByChannel.remove(channel);
        if (clientId != null) {
            MqttClientSession session = sessionsByClientId.remove(clientId);
            if (session != null && !session.isCleanSession()) {
                durableSubscriptionsByClientId.put(clientId, new ConcurrentHashMap<>(session.getSubscriptions()));
                persistSubscriptions(clientId, session.getSubscriptions());
            } else {
                durableSubscriptionsByClientId.remove(clientId);
                redisTemplate.delete(subscriptionKey(clientId));
            }
        }
    }

    public void subscribe(Channel channel, String topicFilter, MqttQoS qoS) {
        findByChannel(channel).ifPresent(session -> {
            session.getSubscriptions().put(topicFilter, qoS);
            if (!session.isCleanSession()) {
                upsertSubscription(session.getClientId(), topicFilter, qoS);
            }
        });
    }

    public void unsubscribe(Channel channel, List<String> topicFilters) {
        findByChannel(channel).ifPresent(session -> topicFilters.forEach(topicFilter -> {
            session.getSubscriptions().remove(topicFilter);
            redisTemplate.opsForHash().delete(subscriptionKey(session.getClientId()), topicFilter);
        }));
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

    private Map<String, MqttQoS> loadSubscriptions(String clientId) {
        Map<String, MqttQoS> subscriptions = new ConcurrentHashMap<>();
        redisTemplate.opsForHash().entries(subscriptionKey(clientId))
            .forEach((topicFilter, qoS) -> subscriptions.put(String.valueOf(topicFilter), MqttQoS.valueOf(Integer.parseInt(String.valueOf(qoS)))));
        return subscriptions;
    }

    private void persistSubscriptions(String clientId, Map<String, MqttQoS> subscriptions) {
        redisTemplate.delete(subscriptionKey(clientId));
        subscriptions.forEach((topicFilter, qoS) -> upsertSubscription(clientId, topicFilter, qoS));
    }

    private void upsertSubscription(String clientId, String topicFilter, MqttQoS qoS) {
        redisTemplate.opsForHash().put(subscriptionKey(clientId), topicFilter, String.valueOf(qoS.value()));
    }

    private String subscriptionKey(String clientId) {
        return "chatpass:mqtt:subscriptions:" + clientId;
    }
}

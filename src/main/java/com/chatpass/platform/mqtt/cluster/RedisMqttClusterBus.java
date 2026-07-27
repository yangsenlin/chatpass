package com.chatpass.platform.mqtt.cluster;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Primary
@Component
public class RedisMqttClusterBus implements MqttClusterBus, MessageListener {

    private final MqttBrokerProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private RedisMessageListenerContainer listenerContainer;

    public RedisMqttClusterBus(
        MqttBrokerProperties properties,
        StringRedisTemplate redisTemplate,
        RedisConnectionFactory connectionFactory,
        ApplicationEventPublisher eventPublisher,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        if (!properties.isClusterEnabled()) {
            return;
        }
        listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.addMessageListener(this, new ChannelTopic(channelName()));
        listenerContainer.afterPropertiesSet();
        listenerContainer.start();
    }

    @PreDestroy
    public void stop() {
        if (listenerContainer != null) {
            listenerContainer.stop();
            try {
                listenerContainer.destroy();
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to destroy MQTT Redis cluster bus listener", ex);
            }
        }
    }

    @Override
    public void broadcast(MqttClusterMessage message) {
        if (!properties.isClusterEnabled()) {
            return;
        }
        redisTemplate.convertAndSend(channelName(), toJson(message));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        eventPublisher.publishEvent(fromJson(new String(message.getBody(), StandardCharsets.UTF_8)));
    }

    private String channelName() {
        return properties.getClusterBusChannelPrefix() + ":" + properties.getClusterId() + ":bus";
    }

    private String toJson(MqttClusterMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize MQTT cluster message", ex);
        }
    }

    private MqttClusterMessage fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, MqttClusterMessage.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize MQTT cluster message", ex);
        }
    }
}

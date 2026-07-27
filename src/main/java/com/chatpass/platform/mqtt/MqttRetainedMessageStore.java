package com.chatpass.platform.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MqttRetainedMessageStore {

    private static final String RETAINED_KEY = "chatpass:mqtt:retained";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MqttRetainedMessageStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(String topic, byte[] payload, MqttQoS qoS) {
        redisTemplate.opsForHash().put(RETAINED_KEY, topic, toJson(new MqttRetainedMessage(topic, payload, qoS)));
    }

    public void delete(String topic) {
        redisTemplate.opsForHash().delete(RETAINED_KEY, topic);
    }

    public List<MqttRetainedMessage> findAll() {
        return redisTemplate.opsForHash().values(RETAINED_KEY).stream()
            .filter(Objects::nonNull)
            .map(value -> fromJson(String.valueOf(value)))
            .toList();
    }

    private String toJson(MqttRetainedMessage message) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "topic", message.getTopic(),
                "payload", message.getPayload(),
                "qos", message.getQoS().value()
            ));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize retained message", ex);
        }
    }

    private MqttRetainedMessage fromJson(String payload) {
        try {
            Map<?, ?> map = objectMapper.readValue(payload, Map.class);
            return new MqttRetainedMessage(
                String.valueOf(map.get("topic")),
                objectMapper.convertValue(map.get("payload"), byte[].class),
                MqttQoS.valueOf(((Number) map.get("qos")).intValue())
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize retained message", ex);
        }
    }
}

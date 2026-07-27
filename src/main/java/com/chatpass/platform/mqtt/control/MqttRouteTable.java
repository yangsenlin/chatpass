package com.chatpass.platform.mqtt.control;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Component
public class MqttRouteTable {

    private static final String CONVERSATION_ROUTE_PREFIX = "chatpass:mqtt:route:conversation:";
    private static final String STREAM_ROUTE_PREFIX = "chatpass:mqtt:route:stream:";

    private final MqttBrokerProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MqttRouteTable(MqttBrokerProperties properties, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public MqttRouteTarget bindConversation(String tenantId, String conversationId, String clusterId) {
        MqttRouteTarget target = routeTarget(tenantId, conversationId, null, clusterId, null);
        redisTemplate.opsForValue().set(conversationKey(tenantId, conversationId), toJson(target));
        return target;
    }

    public MqttRouteTarget bindStream(
        String tenantId,
        String conversationId,
        String streamId,
        String clusterId,
        String nodeId
    ) {
        MqttRouteTarget target = routeTarget(tenantId, conversationId, streamId, clusterId, nodeId);
        redisTemplate.opsForValue().set(streamKey(streamId), toJson(target));
        return target;
    }

    public Optional<MqttRouteTarget> conversationRoute(String tenantId, String conversationId) {
        if (isBlank(tenantId) || isBlank(conversationId)) {
            return Optional.empty();
        }
        String payload = redisTemplate.opsForValue().get(conversationKey(tenantId, conversationId));
        return payload == null ? Optional.empty() : Optional.of(fromJson(payload));
    }

    public Optional<MqttRouteTarget> streamRoute(String streamId) {
        if (isBlank(streamId)) {
            return Optional.empty();
        }
        String payload = redisTemplate.opsForValue().get(streamKey(streamId));
        return payload == null ? Optional.empty() : Optional.of(fromJson(payload));
    }

    private MqttRouteTarget routeTarget(
        String tenantId,
        String conversationId,
        String streamId,
        String clusterId,
        String nodeId
    ) {
        MqttRouteTarget target = new MqttRouteTarget();
        target.setTenantId(tenantId);
        target.setConversationId(conversationId);
        target.setStreamId(streamId);
        target.setClusterId(clusterId);
        target.setShardId(shardId(tenantId, conversationId, streamId));
        target.setNodeId(nodeId);
        target.setUpdatedAt(Instant.now());
        return target;
    }

    private String shardId(String tenantId, String conversationId, String streamId) {
        String source = !isBlank(conversationId) ? conversationId : streamId;
        if (isBlank(source)) {
            source = tenantId;
        }
        int shardCount = Math.max(1, properties.getRouteShardCount());
        return "shard-" + Math.floorMod(Objects.hash(source), shardCount);
    }

    private String conversationKey(String tenantId, String conversationId) {
        return CONVERSATION_ROUTE_PREFIX + tenantId + ":" + conversationId;
    }

    private String streamKey(String streamId) {
        return STREAM_ROUTE_PREFIX + streamId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toJson(MqttRouteTarget target) {
        try {
            return objectMapper.writeValueAsString(target);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize MQTT route target", ex);
        }
    }

    private MqttRouteTarget fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, MqttRouteTarget.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize MQTT route target", ex);
        }
    }
}

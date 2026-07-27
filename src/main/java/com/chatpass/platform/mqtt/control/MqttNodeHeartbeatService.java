package com.chatpass.platform.mqtt.control;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class MqttNodeHeartbeatService {

    private static final String NODE_KEY_PREFIX = "chatpass:mqtt:nodes:";
    private static final String NODE_MEMBERS_KEY_PREFIX = "chatpass:mqtt:node-members:";

    private final MqttBrokerProperties properties;
    private final MqttClusterControlService clusterControlService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MqttNodeHeartbeatService(
        MqttBrokerProperties properties,
        MqttClusterControlService clusterControlService,
        StringRedisTemplate redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.clusterControlService = clusterControlService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        heartbeat();
    }

    @Scheduled(fixedDelayString = "${chatpass.mqtt.heartbeat-interval-ms:10000}")
    public void heartbeat() {
        if (!properties.isEnabled() || !properties.isClusterEnabled()) {
            return;
        }
        MqttNodeHeartbeat heartbeat = localHeartbeat();
        redisTemplate.opsForValue().set(
            nodeKey(heartbeat.getClusterId(), heartbeat.getNodeId()),
            toJson(heartbeat),
            Duration.ofMillis(properties.getHeartbeatTtlMs())
        );
        redisTemplate.opsForSet().add(nodeMembersKey(heartbeat.getClusterId()), heartbeat.getNodeId());
    }

    @Scheduled(fixedDelayString = "${chatpass.mqtt.heartbeat-interval-ms:10000}")
    public void refreshClusterHealth() {
        if (!properties.isEnabled() || !properties.isClusterEnabled()) {
            return;
        }
        clusterControlService.listClusters().forEach(cluster -> {
            if (cluster.getStatus() == MqttClusterStatus.DISABLED || cluster.getStatus() == MqttClusterStatus.DRAINING) {
                return;
            }
            long knownNodes = knownNodeCount(cluster.getClusterId());
            long liveNodes = liveNodes(cluster.getClusterId()).size();
            if (liveNodes == 0) {
                cluster.setStatus(MqttClusterStatus.OFFLINE);
            } else if (knownNodes > liveNodes) {
                cluster.setStatus(MqttClusterStatus.DEGRADED);
            } else {
                cluster.setStatus(MqttClusterStatus.ACTIVE);
            }
            clusterControlService.saveCluster(cluster);
        });
    }

    public List<MqttNodeHeartbeat> liveNodes(String clusterId) {
        var members = redisTemplate.opsForSet().members(nodeMembersKey(clusterId));
        if (members == null) {
            return List.of();
        }
        return members.stream()
            .map(nodeId -> redisTemplate.opsForValue().get(nodeKey(clusterId, nodeId)))
            .filter(Objects::nonNull)
            .map(this::fromJson)
            .toList();
    }

    public Optional<MqttNodeHeartbeat> liveNode(String clusterId, String nodeId) {
        String payload = redisTemplate.opsForValue().get(nodeKey(clusterId, nodeId));
        return payload == null ? Optional.empty() : Optional.of(fromJson(payload));
    }

    private long knownNodeCount(String clusterId) {
        Long count = redisTemplate.opsForSet().size(nodeMembersKey(clusterId));
        return count == null ? 0 : count;
    }

    private MqttNodeHeartbeat localHeartbeat() {
        MqttNodeHeartbeat heartbeat = new MqttNodeHeartbeat();
        heartbeat.setClusterId(properties.getClusterId());
        heartbeat.setNodeId(properties.getNodeId());
        heartbeat.setRegion(properties.getRegion());
        heartbeat.setZone(properties.getZone());
        heartbeat.setEndpoint(properties.getPublicEndpoint());
        heartbeat.setLastSeenAt(Instant.now());
        heartbeat.getMetadata().put("tcpPort", properties.getPort());
        heartbeat.getMetadata().put("websocketPort", properties.getWebsocketPort());
        return heartbeat;
    }

    private String nodeKey(String clusterId, String nodeId) {
        return NODE_KEY_PREFIX + clusterId + ":" + nodeId;
    }

    private String nodeMembersKey(String clusterId) {
        return NODE_MEMBERS_KEY_PREFIX + clusterId;
    }

    private String toJson(MqttNodeHeartbeat heartbeat) {
        try {
            return objectMapper.writeValueAsString(heartbeat);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize MQTT node heartbeat", ex);
        }
    }

    private MqttNodeHeartbeat fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, MqttNodeHeartbeat.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize MQTT node heartbeat", ex);
        }
    }
}

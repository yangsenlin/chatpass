package com.chatpass.platform.mqtt.control;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqttRouteTableTest {

    @Test
    void shouldPersistAndReadConversationRoute() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = valueOperations(redisTemplate);
        MqttRouteTable routeTable = new MqttRouteTable(properties(), redisTemplate, objectMapper());

        MqttRouteTarget saved = routeTable.bindConversation("tenant-1", "conversation-1", "cluster-a");
        when(valueOperations.get("chatpass:mqtt:route:conversation:tenant-1:conversation-1"))
            .thenReturn(toJson(saved));

        MqttRouteTarget loaded = routeTable.conversationRoute("tenant-1", "conversation-1").orElseThrow();

        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
        assertThat(loaded.getConversationId()).isEqualTo("conversation-1");
        assertThat(loaded.getClusterId()).isEqualTo("cluster-a");
        assertThat(loaded.getShardId()).startsWith("shard-");
        verify(valueOperations).set(eq("chatpass:mqtt:route:conversation:tenant-1:conversation-1"), eq(toJson(saved)));
    }

    @Test
    void shouldPersistAndReadStreamRouteWithNode() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = valueOperations(redisTemplate);
        MqttRouteTable routeTable = new MqttRouteTable(properties(), redisTemplate, objectMapper());

        MqttRouteTarget saved = routeTable.bindStream(
            "tenant-1",
            "conversation-1",
            "stream-1",
            "cluster-a",
            "node-1"
        );
        when(valueOperations.get("chatpass:mqtt:route:stream:stream-1")).thenReturn(toJson(saved));

        MqttRouteTarget loaded = routeTable.streamRoute("stream-1").orElseThrow();

        assertThat(loaded.getStreamId()).isEqualTo("stream-1");
        assertThat(loaded.getClusterId()).isEqualTo("cluster-a");
        assertThat(loaded.getNodeId()).isEqualTo("node-1");
        assertThat(loaded.getShardId()).isEqualTo(saved.getShardId());
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOperations(StringRedisTemplate redisTemplate) {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return valueOperations;
    }

    private MqttBrokerProperties properties() {
        MqttBrokerProperties properties = new MqttBrokerProperties();
        properties.setRouteShardCount(16);
        return properties;
    }

    private ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    private String toJson(MqttRouteTarget target) {
        try {
            return objectMapper().writeValueAsString(target);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

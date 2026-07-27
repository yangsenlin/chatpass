package com.chatpass.platform.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChannelIntegrationService {

    private static final String CHANNEL_KEY_PREFIX = "chatpass:tenant:channels:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final TenantService tenantService;

    public ChannelIntegrationService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, TenantService tenantService) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.tenantService = tenantService;
    }

    public ChannelIntegration save(String tenantId, ChannelIntegration integration) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        integration.setTenantId(tenantId);
        if (integration.getIntegrationId() == null || integration.getIntegrationId().isBlank()) {
            integration.setIntegrationId(UUID.randomUUID().toString());
        }
        redisTemplate.opsForHash().put(key(tenantId), integration.getIntegrationId(), toJson(integration));
        return integration;
    }

    public List<ChannelIntegration> list(String tenantId) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return redisTemplate.opsForHash().values(key(tenantId)).stream()
            .map(value -> fromJson(String.valueOf(value)))
            .toList();
    }

    private String key(String tenantId) {
        return CHANNEL_KEY_PREFIX + tenantId;
    }

    private String toJson(ChannelIntegration integration) {
        try {
            return objectMapper.writeValueAsString(integration);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize channel integration", ex);
        }
    }

    private ChannelIntegration fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, ChannelIntegration.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize channel integration", ex);
        }
    }
}

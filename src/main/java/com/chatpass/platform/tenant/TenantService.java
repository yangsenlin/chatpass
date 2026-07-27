package com.chatpass.platform.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private static final String TENANT_KEY = "chatpass:tenants";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public TenantService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Tenant create(Tenant tenant) {
        if (tenant.getTenantId() == null || tenant.getTenantId().isBlank()) {
            tenant.setTenantId(UUID.randomUUID().toString());
        }
        redisTemplate.opsForHash().put(TENANT_KEY, tenant.getTenantId(), toJson(tenant));
        return tenant;
    }

    public Optional<Tenant> get(String tenantId) {
        Object payload = redisTemplate.opsForHash().get(TENANT_KEY, tenantId);
        return payload == null ? Optional.empty() : Optional.of(fromJson(String.valueOf(payload)));
    }

    public List<Tenant> list() {
        return redisTemplate.opsForHash().values(TENANT_KEY).stream()
            .map(value -> fromJson(String.valueOf(value)))
            .toList();
    }

    public Tenant suspend(String tenantId) {
        Tenant tenant = get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        tenant.setStatus(TenantStatus.SUSPENDED);
        return create(tenant);
    }

    private String toJson(Tenant tenant) {
        try {
            return objectMapper.writeValueAsString(tenant);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize tenant", ex);
        }
    }

    private Tenant fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, Tenant.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize tenant", ex);
        }
    }
}

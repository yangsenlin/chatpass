package com.chatpass.platform.mqtt.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class MqttTenantMigrationService {

    private static final String MIGRATION_KEY = "chatpass:mqtt:migrations";
    private static final String ACTIVE_MIGRATION_KEY = "chatpass:mqtt:tenant-active-migrations";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MqttTenantMigrationService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public MqttTenantMigrationPlan save(MqttTenantMigrationPlan plan) {
        plan.setUpdatedAt(Instant.now());
        redisTemplate.opsForHash().put(MIGRATION_KEY, plan.getMigrationId(), toJson(plan));
        if (plan.getStatus() == MqttTenantMigrationStatus.RUNNING) {
            redisTemplate.opsForHash().put(ACTIVE_MIGRATION_KEY, plan.getTenantId(), plan.getMigrationId());
        } else {
            redisTemplate.opsForHash().delete(ACTIVE_MIGRATION_KEY, plan.getTenantId());
        }
        return plan;
    }

    public Optional<MqttTenantMigrationPlan> get(String migrationId) {
        Object payload = redisTemplate.opsForHash().get(MIGRATION_KEY, migrationId);
        return payload == null ? Optional.empty() : Optional.of(fromJson(String.valueOf(payload)));
    }

    public Optional<MqttTenantMigrationPlan> active(String tenantId) {
        Object migrationId = redisTemplate.opsForHash().get(ACTIVE_MIGRATION_KEY, tenantId);
        if (migrationId == null) {
            return Optional.empty();
        }
        return get(String.valueOf(migrationId))
            .filter(plan -> plan.getStatus() == MqttTenantMigrationStatus.RUNNING);
    }

    private String toJson(MqttTenantMigrationPlan plan) {
        try {
            return objectMapper.writeValueAsString(plan);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize MQTT tenant migration", ex);
        }
    }

    private MqttTenantMigrationPlan fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, MqttTenantMigrationPlan.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize MQTT tenant migration", ex);
        }
    }
}

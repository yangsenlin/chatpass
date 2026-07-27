package com.chatpass.platform.mqtt.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqttTenantMigrationServiceTest {

    private static final String MIGRATION_KEY = "chatpass:mqtt:migrations";
    private static final String ACTIVE_MIGRATION_KEY = "chatpass:mqtt:tenant-active-migrations";

    @Test
    void shouldStoreRunningMigrationAsActive() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        HashOperations<String, Object, Object> hashOperations = hashOperations(redisTemplate);
        MqttTenantMigrationService service = new MqttTenantMigrationService(redisTemplate, objectMapper());
        MqttTenantMigrationPlan plan = plan();

        service.save(plan);
        when(hashOperations.get(ACTIVE_MIGRATION_KEY, "tenant-1")).thenReturn("migration-1");
        when(hashOperations.get(MIGRATION_KEY, "migration-1")).thenReturn(toJson(plan));

        MqttTenantMigrationPlan active = service.active("tenant-1").orElseThrow();

        assertThat(active.getMigrationId()).isEqualTo("migration-1");
        assertThat(active.getSourceClusterId()).isEqualTo("cluster-a");
        assertThat(active.getTargetClusterId()).isEqualTo("cluster-b");
        verify(hashOperations).put(ACTIVE_MIGRATION_KEY, "tenant-1", "migration-1");
    }

    @Test
    void shouldClearActiveMigrationWhenCompleted() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        HashOperations<String, Object, Object> hashOperations = hashOperations(redisTemplate);
        MqttTenantMigrationService service = new MqttTenantMigrationService(redisTemplate, objectMapper());
        MqttTenantMigrationPlan plan = plan();

        plan.setStatus(MqttTenantMigrationStatus.COMPLETED);
        service.save(plan);

        verify(hashOperations).delete(ACTIVE_MIGRATION_KEY, "tenant-1");
    }

    @SuppressWarnings("unchecked")
    private HashOperations<String, Object, Object> hashOperations(StringRedisTemplate redisTemplate) {
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        return hashOperations;
    }

    private MqttTenantMigrationPlan plan() {
        MqttTenantMigrationPlan plan = new MqttTenantMigrationPlan();
        plan.setMigrationId("migration-1");
        plan.setTenantId("tenant-1");
        plan.setSourceClusterId("cluster-a");
        plan.setTargetClusterId("cluster-b");
        return plan;
    }

    private ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    private String toJson(MqttTenantMigrationPlan plan) {
        try {
            return objectMapper().writeValueAsString(plan);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

package com.chatpass.platform.mqtt.control;

import com.chatpass.platform.tenant.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class MqttClusterControlService {

    private static final String CLUSTER_KEY = "chatpass:mqtt:clusters";
    private static final String TENANT_BINDING_KEY = "chatpass:mqtt:tenant-bindings";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final TenantService tenantService;
    private final MqttRouteTable routeTable;
    private final MqttTenantMigrationService migrationService;

    public MqttClusterControlService(
        StringRedisTemplate redisTemplate,
        ObjectMapper objectMapper,
        TenantService tenantService,
        MqttRouteTable routeTable,
        MqttTenantMigrationService migrationService
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.tenantService = tenantService;
        this.routeTable = routeTable;
        this.migrationService = migrationService;
    }

    public MqttCluster saveCluster(MqttCluster cluster) {
        if (cluster.getClusterId() == null || cluster.getClusterId().isBlank()) {
            throw new IllegalArgumentException("clusterId is required");
        }
        redisTemplate.opsForHash().put(CLUSTER_KEY, cluster.getClusterId(), toJson(cluster));
        return cluster;
    }

    public List<MqttCluster> listClusters() {
        return redisTemplate.opsForHash().values(CLUSTER_KEY).stream()
            .map(value -> fromJson(String.valueOf(value), MqttCluster.class))
            .sorted(Comparator.comparing(MqttCluster::getClusterId))
            .toList();
    }

    public Optional<MqttCluster> getCluster(String clusterId) {
        Object payload = redisTemplate.opsForHash().get(CLUSTER_KEY, clusterId);
        return payload == null ? Optional.empty() : Optional.of(fromJson(String.valueOf(payload), MqttCluster.class));
    }

    public TenantClusterBinding bindTenant(String tenantId, TenantClusterBinding binding) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        getCluster(binding.getClusterId()).orElseThrow(() -> new IllegalArgumentException("MQTT cluster not found: " + binding.getClusterId()));
        binding.setTenantId(tenantId);
        redisTemplate.opsForHash().put(TENANT_BINDING_KEY, tenantId, toJson(binding));
        return binding;
    }

    public Optional<TenantClusterBinding> getBinding(String tenantId) {
        Object payload = redisTemplate.opsForHash().get(TENANT_BINDING_KEY, tenantId);
        return payload == null ? Optional.empty() : Optional.of(fromJson(String.valueOf(payload), TenantClusterBinding.class));
    }

    public MqttEndpointResponse endpoint(String tenantId) {
        return endpoint(tenantId, null, null);
    }

    public MqttEndpointResponse endpoint(String tenantId, String conversationId, String streamId) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        Optional<MqttRouteTarget> route = resolveRoute(tenantId, conversationId, streamId);
        if (route.isPresent()) {
            MqttCluster cluster = getCluster(route.get().getClusterId())
                .orElseThrow(() -> new IllegalArgumentException("MQTT cluster not found: " + route.get().getClusterId()));
            return response(tenantId, cluster, route.get());
        }

        MqttCluster cluster = endpointCluster(tenantId);
        MqttRouteTarget createdRoute = null;
        if (!isBlank(conversationId)) {
            createdRoute = routeTable.bindConversation(tenantId, conversationId, cluster.getClusterId());
        }
        return response(tenantId, cluster, createdRoute);
    }

    public MqttTenantMigrationPlan startTenantMigration(String tenantId, MqttTenantMigrationRequest request) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        if (request.getTargetClusterId() == null || request.getTargetClusterId().isBlank()) {
            throw new IllegalArgumentException("targetClusterId is required");
        }
        TenantClusterBinding binding = getBinding(tenantId).orElseGet(() -> autoBind(tenantId));
        MqttCluster sourceCluster = getCluster(binding.getClusterId())
            .orElseThrow(() -> new IllegalArgumentException("MQTT source cluster not found: " + binding.getClusterId()));
        MqttCluster targetCluster = getCluster(request.getTargetClusterId())
            .orElseThrow(() -> new IllegalArgumentException("MQTT target cluster not found: " + request.getTargetClusterId()));
        if (targetCluster.getStatus() != MqttClusterStatus.ACTIVE) {
            throw new IllegalArgumentException("MQTT target cluster is not active: " + request.getTargetClusterId());
        }
        if (sourceCluster.getClusterId().equals(targetCluster.getClusterId())) {
            throw new IllegalArgumentException("sourceClusterId and targetClusterId must be different");
        }
        if (request.isDrainSource()) {
            sourceCluster.setStatus(MqttClusterStatus.DRAINING);
            saveCluster(sourceCluster);
        }

        MqttTenantMigrationPlan plan = new MqttTenantMigrationPlan();
        plan.setMigrationId(UUID.randomUUID().toString());
        plan.setTenantId(tenantId);
        plan.setSourceClusterId(sourceCluster.getClusterId());
        plan.setTargetClusterId(targetCluster.getClusterId());
        plan.setDrainSource(request.isDrainSource());
        plan.setSwitchBindingOnComplete(request.isSwitchBindingOnComplete());
        return migrationService.save(plan);
    }

    public Optional<MqttTenantMigrationPlan> activeTenantMigration(String tenantId) {
        return migrationService.active(tenantId);
    }

    public MqttTenantMigrationPlan completeTenantMigration(String tenantId, String migrationId) {
        MqttTenantMigrationPlan plan = migration(tenantId, migrationId);
        if (plan.isSwitchBindingOnComplete()) {
            TenantClusterBinding binding = getBinding(tenantId).orElseGet(TenantClusterBinding::new);
            binding.setClusterId(plan.getTargetClusterId());
            binding.setStrategy("TENANT_STICKY");
            bindTenant(tenantId, binding);
        }
        plan.setStatus(MqttTenantMigrationStatus.COMPLETED);
        return migrationService.save(plan);
    }

    public MqttTenantMigrationPlan cancelTenantMigration(String tenantId, String migrationId) {
        MqttTenantMigrationPlan plan = migration(tenantId, migrationId);
        if (plan.isDrainSource()) {
            getCluster(plan.getSourceClusterId()).ifPresent(cluster -> {
                if (cluster.getStatus() == MqttClusterStatus.DRAINING) {
                    cluster.setStatus(MqttClusterStatus.ACTIVE);
                    saveCluster(cluster);
                }
            });
        }
        plan.setStatus(MqttTenantMigrationStatus.CANCELLED);
        return migrationService.save(plan);
    }

    private MqttTenantMigrationPlan migration(String tenantId, String migrationId) {
        MqttTenantMigrationPlan plan = migrationService.get(migrationId)
            .orElseThrow(() -> new IllegalArgumentException("MQTT tenant migration not found: " + migrationId));
        if (!tenantId.equals(plan.getTenantId())) {
            throw new IllegalArgumentException("MQTT tenant migration does not belong to tenant: " + tenantId);
        }
        if (plan.getStatus() != MqttTenantMigrationStatus.RUNNING) {
            throw new IllegalArgumentException("MQTT tenant migration is not running: " + migrationId);
        }
        return plan;
    }

    private MqttCluster endpointCluster(String tenantId) {
        Optional<MqttTenantMigrationPlan> migration = migrationService.active(tenantId);
        if (migration.isPresent()) {
            return getCluster(migration.get().getTargetClusterId())
                .orElseThrow(() -> new IllegalArgumentException("MQTT target cluster not found: " + migration.get().getTargetClusterId()));
        }
        TenantClusterBinding binding = getBinding(tenantId).orElseGet(() -> autoBind(tenantId));
        return getCluster(binding.getClusterId())
            .orElseThrow(() -> new IllegalArgumentException("MQTT cluster not found: " + binding.getClusterId()));
    }

    private Optional<MqttRouteTarget> resolveRoute(String tenantId, String conversationId, String streamId) {
        return routeTable.streamRoute(streamId).or(() -> routeTable.conversationRoute(tenantId, conversationId));
    }

    private MqttEndpointResponse response(String tenantId, MqttCluster cluster, MqttRouteTarget route) {
        MqttEndpointResponse response = new MqttEndpointResponse();
        response.setTenantId(tenantId);
        response.setClusterId(cluster.getClusterId());
        if (route != null) {
            response.setShardId(route.getShardId());
            response.setNodeId(route.getNodeId());
        }
        response.setEndpoint(cluster.getEndpoint());
        response.setBackupEndpoints(cluster.getBackupEndpoints());
        return response;
    }

    private TenantClusterBinding autoBind(String tenantId) {
        MqttCluster cluster = listClusters().stream()
            .filter(item -> item.getStatus() == MqttClusterStatus.ACTIVE)
            .max(Comparator.comparingLong(item -> rendezvousScore(tenantId, item)))
            .orElseThrow(() -> new IllegalStateException("No active MQTT cluster available"));
        TenantClusterBinding binding = new TenantClusterBinding();
        binding.setTenantId(tenantId);
        binding.setClusterId(cluster.getClusterId());
        return bindTenant(tenantId, binding);
    }

    private long rendezvousScore(String tenantId, MqttCluster cluster) {
        long hash = Integer.toUnsignedLong(Objects.hash(tenantId, cluster.getClusterId()));
        return hash * Math.max(1, cluster.getWeight());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize MQTT control payload", ex);
        }
    }

    private <T> T fromJson(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize MQTT control payload", ex);
        }
    }
}

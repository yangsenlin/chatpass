package com.chatpass.platform.api;

import com.chatpass.platform.mqtt.control.MqttCluster;
import com.chatpass.platform.mqtt.control.MqttClusterControlService;
import com.chatpass.platform.mqtt.control.MqttEndpointResponse;
import com.chatpass.platform.mqtt.control.MqttNodeHeartbeat;
import com.chatpass.platform.mqtt.control.MqttNodeHeartbeatService;
import com.chatpass.platform.mqtt.control.MqttRouteTable;
import com.chatpass.platform.mqtt.control.MqttRouteTarget;
import com.chatpass.platform.mqtt.control.MqttTenantMigrationPlan;
import com.chatpass.platform.mqtt.control.MqttTenantMigrationRequest;
import com.chatpass.platform.mqtt.control.TenantClusterBinding;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mqtt")
public class MqttControlPlaneController {

    private final MqttClusterControlService clusterControlService;
    private final MqttNodeHeartbeatService nodeHeartbeatService;
    private final MqttRouteTable routeTable;

    public MqttControlPlaneController(
        MqttClusterControlService clusterControlService,
        MqttNodeHeartbeatService nodeHeartbeatService,
        MqttRouteTable routeTable
    ) {
        this.clusterControlService = clusterControlService;
        this.nodeHeartbeatService = nodeHeartbeatService;
        this.routeTable = routeTable;
    }

    @PostMapping("/clusters")
    public MqttCluster saveCluster(@RequestBody MqttCluster cluster) {
        return clusterControlService.saveCluster(cluster);
    }

    @GetMapping("/clusters")
    public List<MqttCluster> listClusters() {
        return clusterControlService.listClusters();
    }

    @GetMapping("/clusters/{clusterId}")
    public MqttCluster getCluster(@PathVariable String clusterId) {
        return clusterControlService.getCluster(clusterId)
            .orElseThrow(() -> new IllegalArgumentException("MQTT cluster not found: " + clusterId));
    }

    @PostMapping("/tenants/{tenantId}/binding")
    public TenantClusterBinding bindTenant(
        @PathVariable String tenantId,
        @RequestBody TenantClusterBinding binding
    ) {
        return clusterControlService.bindTenant(tenantId, binding);
    }

    @GetMapping("/tenants/{tenantId}/binding")
    public TenantClusterBinding getBinding(@PathVariable String tenantId) {
        return clusterControlService.getBinding(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("MQTT tenant binding not found: " + tenantId));
    }

    @GetMapping("/tenants/{tenantId}/endpoint")
    public MqttEndpointResponse endpoint(
        @PathVariable String tenantId,
        @RequestParam(required = false) String conversationId,
        @RequestParam(required = false) String streamId
    ) {
        return clusterControlService.endpoint(tenantId, conversationId, streamId);
    }

    @GetMapping("/clusters/{clusterId}/nodes")
    public List<MqttNodeHeartbeat> listNodes(@PathVariable String clusterId) {
        return nodeHeartbeatService.liveNodes(clusterId);
    }

    @GetMapping("/routes/conversations/{tenantId}/{conversationId}")
    public MqttRouteTarget conversationRoute(@PathVariable String tenantId, @PathVariable String conversationId) {
        return routeTable.conversationRoute(tenantId, conversationId)
            .orElseThrow(() -> new IllegalArgumentException("MQTT conversation route not found: " + conversationId));
    }

    @GetMapping("/routes/streams/{streamId}")
    public MqttRouteTarget streamRoute(@PathVariable String streamId) {
        return routeTable.streamRoute(streamId)
            .orElseThrow(() -> new IllegalArgumentException("MQTT stream route not found: " + streamId));
    }

    @PostMapping("/tenants/{tenantId}/migrations")
    public MqttTenantMigrationPlan startMigration(
        @PathVariable String tenantId,
        @RequestBody MqttTenantMigrationRequest request
    ) {
        return clusterControlService.startTenantMigration(tenantId, request);
    }

    @GetMapping("/tenants/{tenantId}/migrations/active")
    public MqttTenantMigrationPlan activeMigration(@PathVariable String tenantId) {
        return clusterControlService.activeTenantMigration(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("MQTT active tenant migration not found: " + tenantId));
    }

    @PostMapping("/tenants/{tenantId}/migrations/{migrationId}/complete")
    public MqttTenantMigrationPlan completeMigration(
        @PathVariable String tenantId,
        @PathVariable String migrationId
    ) {
        return clusterControlService.completeTenantMigration(tenantId, migrationId);
    }

    @PostMapping("/tenants/{tenantId}/migrations/{migrationId}/cancel")
    public MqttTenantMigrationPlan cancelMigration(
        @PathVariable String tenantId,
        @PathVariable String migrationId
    ) {
        return clusterControlService.cancelTenantMigration(tenantId, migrationId);
    }
}

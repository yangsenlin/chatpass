package com.chatpass.platform.mqtt.control;

import java.util.ArrayList;
import java.util.List;

public class MqttEndpointResponse {

    private String tenantId;
    private String clusterId;
    private String shardId;
    private String nodeId;
    private String endpoint;
    private List<String> backupEndpoints = new ArrayList<>();
    private int expiresIn = 300;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getShardId() {
        return shardId;
    }

    public void setShardId(String shardId) {
        this.shardId = shardId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<String> getBackupEndpoints() {
        return backupEndpoints;
    }

    public void setBackupEndpoints(List<String> backupEndpoints) {
        this.backupEndpoints = backupEndpoints == null ? new ArrayList<>() : backupEndpoints;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}

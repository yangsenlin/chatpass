package com.chatpass.platform.mqtt.control;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MqttCluster {

    private String clusterId;
    private String region;
    private String zone;
    private MqttClusterStatus status = MqttClusterStatus.ACTIVE;
    private String endpoint;
    private List<String> backupEndpoints = new ArrayList<>();
    private int weight = 100;
    private Instant createdAt = Instant.now();

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public MqttClusterStatus getStatus() {
        return status;
    }

    public void setStatus(MqttClusterStatus status) {
        this.status = status;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

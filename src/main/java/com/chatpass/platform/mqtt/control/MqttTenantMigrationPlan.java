package com.chatpass.platform.mqtt.control;

import java.time.Instant;

public class MqttTenantMigrationPlan {

    private String migrationId;
    private String tenantId;
    private String sourceClusterId;
    private String targetClusterId;
    private MqttTenantMigrationStatus status = MqttTenantMigrationStatus.RUNNING;
    private boolean drainSource = true;
    private boolean switchBindingOnComplete = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public String getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(String migrationId) {
        this.migrationId = migrationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSourceClusterId() {
        return sourceClusterId;
    }

    public void setSourceClusterId(String sourceClusterId) {
        this.sourceClusterId = sourceClusterId;
    }

    public String getTargetClusterId() {
        return targetClusterId;
    }

    public void setTargetClusterId(String targetClusterId) {
        this.targetClusterId = targetClusterId;
    }

    public MqttTenantMigrationStatus getStatus() {
        return status;
    }

    public void setStatus(MqttTenantMigrationStatus status) {
        this.status = status;
    }

    public boolean isDrainSource() {
        return drainSource;
    }

    public void setDrainSource(boolean drainSource) {
        this.drainSource = drainSource;
    }

    public boolean isSwitchBindingOnComplete() {
        return switchBindingOnComplete;
    }

    public void setSwitchBindingOnComplete(boolean switchBindingOnComplete) {
        this.switchBindingOnComplete = switchBindingOnComplete;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

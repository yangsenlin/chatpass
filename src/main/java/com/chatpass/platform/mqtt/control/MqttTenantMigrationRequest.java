package com.chatpass.platform.mqtt.control;

public class MqttTenantMigrationRequest {

    private String targetClusterId;
    private boolean drainSource = true;
    private boolean switchBindingOnComplete = true;

    public String getTargetClusterId() {
        return targetClusterId;
    }

    public void setTargetClusterId(String targetClusterId) {
        this.targetClusterId = targetClusterId;
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
}

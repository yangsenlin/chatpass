package com.chatpass.platform.tenant;

import com.chatpass.platform.message.ChannelType;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChannelIntegration {

    private String integrationId;
    private String tenantId;
    private ChannelType channel;
    private boolean enabled = true;
    private Map<String, Object> config = new LinkedHashMap<>();
    private Instant createdAt = Instant.now();

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config == null ? new LinkedHashMap<>() : config;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

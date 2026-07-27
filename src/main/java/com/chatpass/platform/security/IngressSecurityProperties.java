package com.chatpass.platform.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "chatpass.security.ingress")
public class IngressSecurityProperties {

    private boolean enabled = true;

    private String apiKey = "dev-chatpass-key";

    private Map<String, String> webhookSecrets = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Map<String, String> getWebhookSecrets() {
        return webhookSecrets;
    }

    public void setWebhookSecrets(Map<String, String> webhookSecrets) {
        this.webhookSecrets = webhookSecrets == null ? new LinkedHashMap<>() : webhookSecrets;
    }
}

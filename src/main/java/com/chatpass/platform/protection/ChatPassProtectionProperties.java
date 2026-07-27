package com.chatpass.platform.protection;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chatpass.protection")
public class ChatPassProtectionProperties {

    private boolean rateLimitEnabled = true;
    private long ingressRateLimitPerMinute = 3000;
    private long webhookRateLimitPerMinute = 3000;
    private int maxMessageBytes = 1024 * 1024;

    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    public void setRateLimitEnabled(boolean rateLimitEnabled) {
        this.rateLimitEnabled = rateLimitEnabled;
    }

    public long getIngressRateLimitPerMinute() {
        return ingressRateLimitPerMinute;
    }

    public void setIngressRateLimitPerMinute(long ingressRateLimitPerMinute) {
        this.ingressRateLimitPerMinute = ingressRateLimitPerMinute;
    }

    public long getWebhookRateLimitPerMinute() {
        return webhookRateLimitPerMinute;
    }

    public void setWebhookRateLimitPerMinute(long webhookRateLimitPerMinute) {
        this.webhookRateLimitPerMinute = webhookRateLimitPerMinute;
    }

    public int getMaxMessageBytes() {
        return maxMessageBytes;
    }

    public void setMaxMessageBytes(int maxMessageBytes) {
        this.maxMessageBytes = maxMessageBytes;
    }
}

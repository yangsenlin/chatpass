package com.chatpass.platform.api;

import com.chatpass.platform.buffer.BufferedMessage;
import com.chatpass.platform.buffer.MessageBuffer;
import com.chatpass.platform.channel.ChannelMessageAdapterRegistry;
import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.protection.ChatPassMetrics;
import com.chatpass.platform.protection.ChatPassProtectionProperties;
import com.chatpass.platform.protection.MessageSizeValidator;
import com.chatpass.platform.protection.RateLimitExceededException;
import com.chatpass.platform.protection.RedisRateLimiter;
import com.chatpass.platform.security.IngressSecurityVerifier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
public class ChannelWebhookController {

    private final ObjectMapper objectMapper;
    private final ChannelMessageAdapterRegistry adapterRegistry;
    private final IngressSecurityVerifier securityVerifier;
    private final MessageBuffer messageBuffer;
    private final ChatPassProtectionProperties protectionProperties;
    private final MessageSizeValidator messageSizeValidator;
    private final RedisRateLimiter rateLimiter;
    private final ChatPassMetrics metrics;

    public ChannelWebhookController(
        ObjectMapper objectMapper,
        ChannelMessageAdapterRegistry adapterRegistry,
        IngressSecurityVerifier securityVerifier,
        MessageBuffer messageBuffer,
        ChatPassProtectionProperties protectionProperties,
        MessageSizeValidator messageSizeValidator,
        RedisRateLimiter rateLimiter,
        ChatPassMetrics metrics
    ) {
        this.objectMapper = objectMapper;
        this.adapterRegistry = adapterRegistry;
        this.securityVerifier = securityVerifier;
        this.messageBuffer = messageBuffer;
        this.protectionProperties = protectionProperties;
        this.messageSizeValidator = messageSizeValidator;
        this.rateLimiter = rateLimiter;
        this.metrics = metrics;
    }

    @PostMapping("/{channel}/webhook")
    public BufferedMessage webhook(
        @PathVariable ChannelType channel,
        @RequestHeader HttpHeaders headers,
        @RequestBody String rawBody
    ) throws Exception {
        protectWebhook("webhook:" + channel.name(), rawBody);
        securityVerifier.verify(channel, headers, rawBody);
        Map<String, Object> payload = objectMapper.readValue(rawBody, new TypeReference<>() {
        });
        UnifiedMessage message = adapterRegistry.get(channel).normalize(channel, payload);
        metrics.incrementWebhookMessages();
        return messageBuffer.enqueue(message);
    }

    @PostMapping("/tenants/{tenantId}/{channel}/webhook")
    public BufferedMessage tenantWebhook(
        @PathVariable String tenantId,
        @PathVariable ChannelType channel,
        @RequestHeader HttpHeaders headers,
        @RequestBody String rawBody
    ) throws Exception {
        protectWebhook("webhook:" + tenantId + ":" + channel.name(), rawBody);
        securityVerifier.verify(channel, headers, rawBody);
        Map<String, Object> payload = objectMapper.readValue(rawBody, new TypeReference<>() {
        });
        UnifiedMessage message = adapterRegistry.get(channel).normalize(channel, payload);
        message.setTenantId(tenantId);
        metrics.incrementWebhookMessages();
        return messageBuffer.enqueue(message);
    }

    private void protectWebhook(String key, String rawBody) {
        messageSizeValidator.validateText("channel webhook", rawBody);
        if (!rateLimiter.allow(key, protectionProperties.getWebhookRateLimitPerMinute(), Duration.ofMinutes(1))) {
            metrics.incrementRateLimitedRequests();
            throw new RateLimitExceededException("channel webhook rate limit exceeded");
        }
    }

    @GetMapping("/buffer/{bufferId}")
    public Object result(@PathVariable String bufferId) {
        return messageBuffer.result(bufferId)
            .<Object>map(result -> result)
            .orElseGet(() -> Map.of("status", "PENDING"));
    }
}

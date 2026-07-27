package com.chatpass.platform.api;

import com.chatpass.platform.buffer.BufferedMessage;
import com.chatpass.platform.buffer.MessageBuffer;
import com.chatpass.platform.channel.ChannelMessageAdapterRegistry;
import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;
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

import java.util.Map;

@RestController
@RequestMapping("/api/channels")
public class ChannelWebhookController {

    private final ObjectMapper objectMapper;
    private final ChannelMessageAdapterRegistry adapterRegistry;
    private final IngressSecurityVerifier securityVerifier;
    private final MessageBuffer messageBuffer;

    public ChannelWebhookController(
        ObjectMapper objectMapper,
        ChannelMessageAdapterRegistry adapterRegistry,
        IngressSecurityVerifier securityVerifier,
        MessageBuffer messageBuffer
    ) {
        this.objectMapper = objectMapper;
        this.adapterRegistry = adapterRegistry;
        this.securityVerifier = securityVerifier;
        this.messageBuffer = messageBuffer;
    }

    @PostMapping("/{channel}/webhook")
    public BufferedMessage webhook(
        @PathVariable ChannelType channel,
        @RequestHeader HttpHeaders headers,
        @RequestBody String rawBody
    ) throws Exception {
        securityVerifier.verify(channel, headers, rawBody);
        Map<String, Object> payload = objectMapper.readValue(rawBody, new TypeReference<>() {
        });
        UnifiedMessage message = adapterRegistry.get(channel).normalize(channel, payload);
        return messageBuffer.enqueue(message);
    }

    @PostMapping("/tenants/{tenantId}/{channel}/webhook")
    public BufferedMessage tenantWebhook(
        @PathVariable String tenantId,
        @PathVariable ChannelType channel,
        @RequestHeader HttpHeaders headers,
        @RequestBody String rawBody
    ) throws Exception {
        securityVerifier.verify(channel, headers, rawBody);
        Map<String, Object> payload = objectMapper.readValue(rawBody, new TypeReference<>() {
        });
        UnifiedMessage message = adapterRegistry.get(channel).normalize(channel, payload);
        message.setTenantId(tenantId);
        return messageBuffer.enqueue(message);
    }

    @GetMapping("/buffer/{bufferId}")
    public Object result(@PathVariable String bufferId) {
        return messageBuffer.result(bufferId)
            .<Object>map(result -> result)
            .orElseGet(() -> Map.of("status", "PENDING"));
    }
}

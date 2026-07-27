package com.chatpass.platform.api;

import com.chatpass.platform.message.MessageIngressService;
import com.chatpass.platform.message.MessageProcessingResult;
import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.protection.ChatPassMetrics;
import com.chatpass.platform.protection.ChatPassProtectionProperties;
import com.chatpass.platform.protection.MessageSizeValidator;
import com.chatpass.platform.protection.RateLimitExceededException;
import com.chatpass.platform.protection.RedisRateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/messages")
public class MessageIngressController {

    private final MessageIngressService messageIngressService;
    private final ObjectMapper objectMapper;
    private final ChatPassProtectionProperties protectionProperties;
    private final MessageSizeValidator messageSizeValidator;
    private final RedisRateLimiter rateLimiter;
    private final ChatPassMetrics metrics;

    public MessageIngressController(
        MessageIngressService messageIngressService,
        ObjectMapper objectMapper,
        ChatPassProtectionProperties protectionProperties,
        MessageSizeValidator messageSizeValidator,
        RedisRateLimiter rateLimiter,
        ChatPassMetrics metrics
    ) {
        this.messageIngressService = messageIngressService;
        this.objectMapper = objectMapper;
        this.protectionProperties = protectionProperties;
        this.messageSizeValidator = messageSizeValidator;
        this.rateLimiter = rateLimiter;
        this.metrics = metrics;
    }

    @PostMapping("/ingress")
    public MessageProcessingResult ingress(@RequestBody String rawBody) throws Exception {
        messageSizeValidator.validateText("message ingress", rawBody);
        if (!rateLimiter.allow("rest:ingress:global", protectionProperties.getIngressRateLimitPerMinute(), Duration.ofMinutes(1))) {
            metrics.incrementRateLimitedRequests();
            throw new RateLimitExceededException("message ingress rate limit exceeded");
        }
        UnifiedMessage message = objectMapper.readValue(rawBody, UnifiedMessage.class);
        metrics.incrementIngressMessages();
        return messageIngressService.receive(message);
    }
}

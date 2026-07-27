package com.chatpass.platform.protection;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageSizeValidator {

    private final ChatPassProtectionProperties properties;
    private final ChatPassMetrics metrics;

    public MessageSizeValidator(ChatPassProtectionProperties properties, ChatPassMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }

    public void validateText(String scope, String payload) {
        validateBytes(scope, payload == null ? 0 : payload.getBytes(StandardCharsets.UTF_8).length, properties.getMaxMessageBytes());
    }

    public void validateBytes(String scope, int size, int limit) {
        if (limit > 0 && size > limit) {
            metrics.incrementOversizedMessages();
            throw new MessageTooLargeException(scope + " payload too large: " + size + " bytes, limit " + limit);
        }
    }
}

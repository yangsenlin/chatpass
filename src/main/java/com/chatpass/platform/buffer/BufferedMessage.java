package com.chatpass.platform.buffer;

import com.chatpass.platform.message.UnifiedMessage;

import java.time.Instant;
import java.util.UUID;

public class BufferedMessage {

    private final String bufferId = UUID.randomUUID().toString();
    private final UnifiedMessage message;
    private final Instant receivedAt = Instant.now();

    public BufferedMessage(UnifiedMessage message) {
        this.message = message;
    }

    public String getBufferId() {
        return bufferId;
    }

    public UnifiedMessage getMessage() {
        return message;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}

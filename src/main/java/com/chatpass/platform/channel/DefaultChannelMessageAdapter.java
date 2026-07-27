package com.chatpass.platform.channel;

import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class DefaultChannelMessageAdapter implements ChannelMessageAdapter {

    @Override
    public boolean supports(ChannelType channel) {
        return channel == ChannelType.API || channel == ChannelType.OTHER || channel == ChannelType.MQTT;
    }

    @Override
    public UnifiedMessage normalize(ChannelType channel, Map<String, Object> payload) {
        UnifiedMessage message = new UnifiedMessage();
        message.setMessageId(value(payload, "messageId", UUID.randomUUID().toString()));
        message.setChannel(channel);
        message.setSenderId(value(payload, "senderId", value(payload, "from", "unknown")));
        message.setReceiverId(value(payload, "receiverId", value(payload, "to", null)));
        message.setConversationId(value(payload, "conversationId", value(payload, "threadId", message.getSenderId())));
        message.setText(value(payload, "text", value(payload, "message", "")));
        message.setTimestamp(Instant.now());
        message.setAttributes(attributes(payload));
        message.setRawPayload(new LinkedHashMap<>(payload));
        return message;
    }

    private Map<String, Object> attributes(Map<String, Object> payload) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("source", "webhook");
        Object metadata = payload.get("metadata");
        if (metadata instanceof Map<?, ?> map) {
            map.forEach((key, value) -> attributes.put(String.valueOf(key), value));
        }
        return attributes;
    }

    private String value(Map<String, Object> payload, String key, String defaultValue) {
        Object value = payload.get(key);
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }
}

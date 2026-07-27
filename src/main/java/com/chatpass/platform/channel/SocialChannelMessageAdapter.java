package com.chatpass.platform.channel;

import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class SocialChannelMessageAdapter implements ChannelMessageAdapter {

    private static final Set<ChannelType> SUPPORTED = Set.of(
        ChannelType.FACEBOOK,
        ChannelType.WHATSAPP,
        ChannelType.LINE,
        ChannelType.INSTAGRAM
    );

    @Override
    public boolean supports(ChannelType channel) {
        return SUPPORTED.contains(channel);
    }

    @Override
    public UnifiedMessage normalize(ChannelType channel, Map<String, Object> payload) {
        UnifiedMessage message = new UnifiedMessage();
        message.setMessageId(text(payload, "messageId", text(payload, "id", UUID.randomUUID().toString())));
        message.setChannel(channel);
        message.setTenantId(text(payload, "tenantId", text(payload, "tenant", null)));
        message.setAppId(text(payload, "appId", text(payload, "app", null)));
        message.setSenderId(text(payload, "senderId", text(payload, "from", nested(payload, "sender", "id", "unknown"))));
        message.setReceiverId(text(payload, "receiverId", text(payload, "to", nested(payload, "recipient", "id", null))));
        message.setConversationId(text(payload, "conversationId", text(payload, "threadId", message.getSenderId())));
        message.setText(text(payload, "text", text(payload, "message", nested(payload, "message", "text", ""))));
        message.setTimestamp(Instant.now());
        message.setAttributes(attributes(channel, payload));
        message.setRawPayload(new LinkedHashMap<>(payload));
        return message;
    }

    private Map<String, Object> attributes(ChannelType channel, Map<String, Object> payload) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("source", channel.name().toLowerCase());
        attributes.put("webhookEvent", payload.get("event"));
        Object responseTopic = payload.get("responseTopic");
        if (responseTopic != null) {
            attributes.put("responseTopic", responseTopic);
        }
        return attributes;
    }

    private String text(Map<String, Object> payload, String key, String defaultValue) {
        Object value = payload.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private String nested(Map<String, Object> payload, String parentKey, String childKey, String defaultValue) {
        Object value = payload.get(parentKey);
        if (value instanceof Map<?, ?> map) {
            Object child = map.get(childKey);
            return child == null ? defaultValue : String.valueOf(child);
        }
        return defaultValue;
    }
}

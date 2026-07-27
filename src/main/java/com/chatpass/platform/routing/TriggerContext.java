package com.chatpass.platform.routing;

import com.chatpass.platform.message.UnifiedMessage;

import java.util.LinkedHashMap;
import java.util.Map;

public class TriggerContext {

    private final UnifiedMessage message;
    private final Map<String, Object> values;

    private TriggerContext(UnifiedMessage message, Map<String, Object> values) {
        this.message = message;
        this.values = values;
    }

    public static TriggerContext from(UnifiedMessage message) {
        Map<String, Object> values = new LinkedHashMap<>();
        put(values, "message.id", message.getMessageId());
        put(values, "channel", message.getChannel());
        put(values, "direction", message.getDirection());
        put(values, "sender.id", message.getSenderId());
        put(values, "receiver.id", message.getReceiverId());
        put(values, "conversation.id", message.getConversationId());
        put(values, "content.text", message.getText());
        put(values, "timestamp", message.getTimestamp());
        flatten(values, "attributes", message.getAttributes());
        flatten(values, "raw", message.getRawPayload());
        return new TriggerContext(message, values);
    }

    public UnifiedMessage getMessage() {
        return message;
    }

    public Object get(String field) {
        return values.get(field);
    }

    public Map<String, Object> values() {
        return values;
    }

    private static void put(Map<String, Object> values, String path, Object value) {
        if (value != null) {
            values.put(path, value);
        }
    }

    private static void flatten(Map<String, Object> values, String prefix, Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        source.forEach((key, value) -> {
            String path = prefix + "." + key;
            put(values, path, value);
            if (value instanceof Map<?, ?> nested) {
                flattenNested(values, path, nested);
            }
        });
    }

    private static void flattenNested(Map<String, Object> values, String prefix, Map<?, ?> source) {
        source.forEach((key, value) -> {
            String path = prefix + "." + key;
            put(values, path, value);
            if (value instanceof Map<?, ?> nested) {
                flattenNested(values, path, nested);
            }
        });
    }
}

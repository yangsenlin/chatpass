package com.chatpass.platform.output;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class InMemoryOutputChannelAdapter implements OutputChannelAdapter {

    private final List<OutboundMessage> messages = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void send(OutboundMessage message) {
        messages.add(message);
    }

    public List<OutboundMessage> recentMessages() {
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }
}

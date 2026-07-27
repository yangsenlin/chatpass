package com.chatpass.platform.channel;

import com.chatpass.platform.message.ChannelType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelMessageAdapterRegistry {

    private final List<ChannelMessageAdapter> adapters;

    public ChannelMessageAdapterRegistry(List<ChannelMessageAdapter> adapters) {
        this.adapters = adapters;
    }

    public ChannelMessageAdapter get(ChannelType channel) {
        return adapters.stream()
            .filter(adapter -> adapter.supports(channel))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No channel adapter registered for: " + channel));
    }
}

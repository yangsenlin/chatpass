package com.chatpass.platform.api;

import com.chatpass.platform.output.InMemoryOutputChannelAdapter;
import com.chatpass.platform.output.OutboundMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/outbox")
public class OutboxController {

    private final InMemoryOutputChannelAdapter outputChannelAdapter;

    public OutboxController(InMemoryOutputChannelAdapter outputChannelAdapter) {
        this.outputChannelAdapter = outputChannelAdapter;
    }

    @GetMapping
    public List<OutboundMessage> list() {
        return outputChannelAdapter.recentMessages();
    }
}

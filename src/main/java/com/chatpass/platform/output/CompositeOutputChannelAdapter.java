package com.chatpass.platform.output;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class CompositeOutputChannelAdapter implements OutputChannelAdapter {

    private final InMemoryOutputChannelAdapter inMemoryOutputChannelAdapter;
    private final MqttOutboundChannelSender mqttOutboundChannelSender;

    public CompositeOutputChannelAdapter(
        InMemoryOutputChannelAdapter inMemoryOutputChannelAdapter,
        MqttOutboundChannelSender mqttOutboundChannelSender
    ) {
        this.inMemoryOutputChannelAdapter = inMemoryOutputChannelAdapter;
        this.mqttOutboundChannelSender = mqttOutboundChannelSender;
    }

    @Override
    public void send(OutboundMessage message) {
        inMemoryOutputChannelAdapter.send(message);
        mqttOutboundChannelSender.sendIfMqtt(message);
    }
}

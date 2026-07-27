package com.chatpass.platform.mqtt;

import com.chatpass.platform.mqtt.session.MqttClientSession;
import com.chatpass.platform.mqtt.session.MqttSessionRegistry;
import com.chatpass.platform.mqtt.session.TopicMatcher;
import com.chatpass.platform.mqtt.cluster.MqttClusterBus;
import com.chatpass.platform.mqtt.cluster.MqttClusterMessage;
import com.chatpass.platform.mqtt.offline.MqttOfflineMessage;
import com.chatpass.platform.mqtt.offline.MqttOfflineMessageStore;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MqttBrokerService {

    private final MqttSessionRegistry sessionRegistry;
    private final MqttIngressBridge ingressBridge;
    private final MqttOfflineMessageStore offlineMessageStore;
    private final MqttRetainedMessageStore retainedMessageStore;
    private final MqttClusterBus clusterBus;
    private final MqttBrokerProperties properties;
    private final AtomicInteger packetIds = new AtomicInteger(1);

    public MqttBrokerService(
        MqttSessionRegistry sessionRegistry,
        MqttIngressBridge ingressBridge,
        MqttOfflineMessageStore offlineMessageStore,
        MqttRetainedMessageStore retainedMessageStore,
        MqttClusterBus clusterBus,
        MqttBrokerProperties properties
    ) {
        this.sessionRegistry = sessionRegistry;
        this.ingressBridge = ingressBridge;
        this.offlineMessageStore = offlineMessageStore;
        this.retainedMessageStore = retainedMessageStore;
        this.clusterBus = clusterBus;
        this.properties = properties;
    }

    public void publishFromClient(MqttClientSession publisher, String topic, byte[] payload, MqttQoS qoS, boolean retain) {
        if (retain) {
            if (payload.length == 0) {
                retainedMessageStore.delete(topic);
            } else {
                retainedMessageStore.save(topic, payload, qoS);
            }
        }
        ingressBridge.bridge(publisher, topic, payload, qoS, retain);
        publishLocal(topic, payload, qoS, retain);
        clusterBus.broadcast(new MqttClusterMessage(properties.getNodeId(), topic, payload, qoS, retain));
        persistOffline(topic, payload, qoS);
    }

    public void publishLocal(String topic, byte[] payload, MqttQoS qoS, boolean retain) {
        for (MqttClientSession session : sessionRegistry.subscribers(topic)) {
            MqttQoS deliverQoS = subscribedQoS(session, topic, qoS);
            session.getChannel().writeAndFlush(publishMessage(topic, payload, deliverQoS, retain));
        }
    }

    public void replayOfflineMessages(MqttClientSession session) {
        List<MqttOfflineMessage> messages = offlineMessageStore.drain(session.getClientId());
        messages.forEach(message -> session.getChannel().writeAndFlush(
            publishMessage(message.getTopic(), message.getPayload(), message.getQoS(), false)
        ));
    }

    public void replayRetainedMessages(MqttClientSession session, List<String> topicFilters) {
        retainedMessageStore.findAll().stream()
            .filter(message -> topicFilters.stream().anyMatch(filter -> TopicMatcher.matches(filter, message.getTopic())))
            .forEach(message -> session.getChannel().writeAndFlush(
                publishMessage(message.getTopic(), message.getPayload(), message.getQoS(), true)
            ));
    }

    private MqttQoS subscribedQoS(MqttClientSession session, String topic, MqttQoS publishedQoS) {
        return session.getSubscriptions().entrySet().stream()
            .filter(entry -> TopicMatcher.matches(entry.getKey(), topic))
            .map(Map.Entry::getValue)
            .findFirst()
            .map(qoS -> MqttQoS.valueOf(Math.min(qoS.value(), publishedQoS.value())))
            .orElse(MqttQoS.AT_MOST_ONCE);
    }

    private void persistOffline(String topic, byte[] payload, MqttQoS qoS) {
        if (!properties.isOfflineEnabled() || qoS == MqttQoS.AT_MOST_ONCE) {
            return;
        }
        sessionRegistry.offlineSubscribers(topic).forEach((clientId, subscriptionQoS) ->
            offlineMessageStore.save(clientId, topic, payload, MqttQoS.valueOf(Math.min(qoS.value(), subscriptionQoS.value())))
        );
    }

    @EventListener
    public void onClusterMessage(MqttClusterMessage message) {
        if (properties.getNodeId().equals(message.getSourceNodeId())) {
            return;
        }
        publishLocal(message.getTopic(), message.getPayload(), message.getQoS(), message.isRetain());
        persistOffline(message.getTopic(), message.getPayload(), message.getQoS());
    }

    private MqttPublishMessage publishMessage(String topic, byte[] payload, MqttQoS qoS, boolean retain) {
        MqttMessageBuilders.PublishBuilder builder = MqttMessageBuilders.publish()
            .topicName(topic)
            .qos(qoS)
            .retained(retain)
            .payload(Unpooled.copiedBuffer(payload));
        if (qoS != MqttQoS.AT_MOST_ONCE) {
            builder.messageId(nextPacketId());
        }
        return builder.build();
    }

    private int nextPacketId() {
        int value = packetIds.getAndUpdate(current -> current >= 65535 ? 1 : current + 1);
        return value == 0 ? 1 : value;
    }
}

package com.chatpass.platform.mqtt.handler;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.chatpass.platform.mqtt.MqttBrokerService;
import com.chatpass.platform.mqtt.session.MqttClientSession;
import com.chatpass.platform.mqtt.session.MqttSessionRegistry;
import com.chatpass.platform.protection.ChatPassMetrics;
import com.chatpass.platform.protection.MessageSizeValidator;
import com.chatpass.platform.protection.RedisRateLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
@ChannelHandler.Sharable
public class MqttBrokerChannelHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private final MqttSessionRegistry sessionRegistry;
    private final MqttBrokerService brokerService;
    private final MqttBrokerProperties properties;
    private final RedisRateLimiter rateLimiter;
    private final MessageSizeValidator messageSizeValidator;
    private final ChatPassMetrics metrics;
    private final Map<MqttMessageType, BiConsumer<ChannelHandlerContext, MqttMessage>> handlerMap =
        new EnumMap<>(MqttMessageType.class);

    public MqttBrokerChannelHandler(
        MqttSessionRegistry sessionRegistry,
        MqttBrokerService brokerService,
        MqttBrokerProperties properties,
        RedisRateLimiter rateLimiter,
        MessageSizeValidator messageSizeValidator,
        ChatPassMetrics metrics
    ) {
        this.sessionRegistry = sessionRegistry;
        this.brokerService = brokerService;
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.messageSizeValidator = messageSizeValidator;
        this.metrics = metrics;
        registerHandlers();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage message) {
        MqttMessageType messageType = message.fixedHeader().messageType();
        if (messageType != MqttMessageType.CONNECT && sessionRegistry.findByChannel(ctx.channel()).isEmpty()) {
            ctx.close();
            return;
        }
        BiConsumer<ChannelHandlerContext, MqttMessage> handler = handlerMap.get(messageType);
        if (handler != null) {
            handler.accept(ctx, message);
        }
    }

    private void registerHandlers() {
        handlerMap.put(MqttMessageType.CONNECT, (ctx, message) -> handleConnect(ctx, (MqttConnectMessage) message));
        handlerMap.put(MqttMessageType.SUBSCRIBE, (ctx, message) -> handleSubscribe(ctx, (MqttSubscribeMessage) message));
        handlerMap.put(MqttMessageType.UNSUBSCRIBE, (ctx, message) -> handleUnsubscribe(ctx, (MqttUnsubscribeMessage) message));
        handlerMap.put(MqttMessageType.PUBLISH, (ctx, message) -> handlePublish(ctx, (MqttPublishMessage) message));
        handlerMap.put(MqttMessageType.PUBREL, this::handlePubRel);
        handlerMap.put(MqttMessageType.PUBREC, this::handlePubRec);
        handlerMap.put(MqttMessageType.PUBACK, this::handlePubAck);
        handlerMap.put(MqttMessageType.PUBCOMP, this::handlePubComp);
        handlerMap.put(MqttMessageType.PINGREQ, (ctx, message) -> ctx.writeAndFlush(MqttMessage.PINGRESP));
        handlerMap.put(MqttMessageType.DISCONNECT, (ctx, message) -> ctx.close());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        sessionRegistry.unregister(ctx.channel());
    }

    private void handleConnect(ChannelHandlerContext ctx, MqttConnectMessage message) {
        String clientId = message.payload().clientIdentifier();
        String username = message.payload().userName();
        if (clientId == null || clientId.isBlank()) {
            ctx.writeAndFlush(connAck(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED));
            ctx.close();
            return;
        }
        if (connectionLimitExceeded(username)) {
            metrics.incrementMqttRejectedConnections();
            ctx.writeAndFlush(connAck(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE));
            ctx.close();
            return;
        }
        MqttClientSession session = sessionRegistry.register(
            clientId,
            username,
            message.variableHeader().isCleanSession(),
            ctx.channel()
        );
        ctx.writeAndFlush(connAck(MqttConnectReturnCode.CONNECTION_ACCEPTED));
        brokerService.replayOfflineMessages(session);
    }

    private void handleSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage message) {
        MqttClientSession session = sessionRegistry.findByChannel(ctx.channel()).orElse(null);
        if (session == null) {
            ctx.close();
            return;
        }
        List<MqttTopicSubscription> subscriptions = message.payload().topicSubscriptions();
        subscriptions.forEach(subscription -> sessionRegistry.subscribe(
            ctx.channel(),
            subscription.topicName(),
            subscription.qualityOfService()
        ));
        MqttQoS[] grantedQoS = subscriptions.stream()
            .map(MqttTopicSubscription::qualityOfService)
            .toArray(MqttQoS[]::new);
        MqttSubAckMessage subAck = MqttMessageBuilders.subAck()
            .packetId(message.variableHeader().messageId())
            .addGrantedQoses(grantedQoS)
            .build();
        ctx.writeAndFlush(subAck);
        brokerService.replayRetainedMessages(session, subscriptions.stream()
            .map(MqttTopicSubscription::topicName)
            .toList());
    }

    private void handleUnsubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage message) {
        sessionRegistry.unsubscribe(ctx.channel(), message.payload().topics());
        ctx.writeAndFlush(MqttMessageBuilders.unsubAck()
            .packetId(message.variableHeader().messageId())
            .build());
    }

    private void handlePublish(ChannelHandlerContext ctx, MqttPublishMessage message) {
        MqttClientSession session = sessionRegistry.findByChannel(ctx.channel()).orElse(null);
        if (session == null) {
            ctx.close();
            return;
        }
        byte[] payload = payloadBytes(message.payload());
        if (payload.length > properties.getMaxPayloadBytes()) {
            metrics.incrementOversizedMessages();
            metrics.incrementMqttRejectedMessages();
            ctx.close();
            return;
        }
        if (!rateLimiter.allow("mqtt:publish:" + rateLimitKey(session), properties.getPublishRateLimitPerMinute(), Duration.ofMinutes(1))) {
            metrics.incrementRateLimitedRequests();
            metrics.incrementMqttRejectedMessages();
            ctx.close();
            return;
        }
        messageSizeValidator.validateBytes("mqtt", payload.length, properties.getMaxPayloadBytes());
        MqttFixedHeader fixedHeader = message.fixedHeader();
        brokerService.publishFromClient(
            session,
            message.variableHeader().topicName(),
            payload,
            fixedHeader.qosLevel(),
            fixedHeader.isRetain()
        );
        if (fixedHeader.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            ctx.writeAndFlush(MqttMessageBuilders.pubAck()
                .packetId(message.variableHeader().packetId())
                .build());
        } else if (fixedHeader.qosLevel() == MqttQoS.EXACTLY_ONCE) {
            ctx.writeAndFlush(messageWithId(MqttMessageType.PUBREC, message.variableHeader().packetId()));
        }
        metrics.incrementMqttPublishes();
    }

    private boolean connectionLimitExceeded(String username) {
        if (properties.getMaxConnections() > 0 && sessionRegistry.activeConnectionCount() >= properties.getMaxConnections()) {
            return true;
        }
        String tenantKey = username == null || username.isBlank() ? "anonymous" : username;
        return properties.getMaxConnectionsPerTenant() > 0
            && sessionRegistry.activeConnectionCountByUsername(tenantKey) >= properties.getMaxConnectionsPerTenant();
    }

    private String rateLimitKey(MqttClientSession session) {
        String username = session.getUsername();
        return username == null || username.isBlank() ? session.getClientId() : username;
    }

    private void handlePubRel(ChannelHandlerContext ctx, MqttMessage message) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message.variableHeader();
        ctx.writeAndFlush(messageWithId(MqttMessageType.PUBCOMP, variableHeader.messageId()));
    }

    private void handlePubRec(ChannelHandlerContext ctx, MqttMessage message) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message.variableHeader();
        ctx.writeAndFlush(messageWithId(MqttMessageType.PUBREL, variableHeader.messageId()));
    }

    private void handlePubAck(ChannelHandlerContext ctx, MqttMessage message) {
        // QoS1 outbound delivery state is currently persisted by the broker service layer.
    }

    private void handlePubComp(ChannelHandlerContext ctx, MqttMessage message) {
        // QoS2 outbound delivery completion hook for future durable inflight cleanup.
    }

    private byte[] payloadBytes(ByteBuf payload) {
        byte[] bytes = new byte[payload.readableBytes()];
        payload.getBytes(payload.readerIndex(), bytes);
        return bytes;
    }

    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode) {
        return MqttMessageBuilders.connAck()
            .returnCode(returnCode)
            .sessionPresent(false)
            .build();
    }

    private MqttMessage messageWithId(MqttMessageType messageType, int packetId) {
        MqttQoS qos = messageType == MqttMessageType.PUBREL ? MqttQoS.AT_LEAST_ONCE : MqttQoS.AT_MOST_ONCE;
        return MqttMessageFactory.newMessage(
            new MqttFixedHeader(messageType, false, qos, false, 0),
            MqttMessageIdVariableHeader.from(packetId),
            null
        );
    }
}

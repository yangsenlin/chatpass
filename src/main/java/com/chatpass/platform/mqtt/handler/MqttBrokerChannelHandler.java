package com.chatpass.platform.mqtt.handler;

import com.chatpass.platform.mqtt.MqttBrokerService;
import com.chatpass.platform.mqtt.session.MqttClientSession;
import com.chatpass.platform.mqtt.session.MqttSessionRegistry;
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

import java.util.List;

@Component
@ChannelHandler.Sharable
public class MqttBrokerChannelHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private final MqttSessionRegistry sessionRegistry;
    private final MqttBrokerService brokerService;

    public MqttBrokerChannelHandler(MqttSessionRegistry sessionRegistry, MqttBrokerService brokerService) {
        this.sessionRegistry = sessionRegistry;
        this.brokerService = brokerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage message) {
        MqttMessageType messageType = message.fixedHeader().messageType();
        switch (messageType) {
            case CONNECT:
                handleConnect(ctx, (MqttConnectMessage) message);
                break;
            case SUBSCRIBE:
                handleSubscribe(ctx, (MqttSubscribeMessage) message);
                break;
            case UNSUBSCRIBE:
                handleUnsubscribe(ctx, (MqttUnsubscribeMessage) message);
                break;
            case PUBLISH:
                handlePublish(ctx, (MqttPublishMessage) message);
                break;
            case PUBREL:
                handlePubRel(ctx, message);
                break;
            case PINGREQ:
                ctx.writeAndFlush(MqttMessage.PINGRESP);
                break;
            case DISCONNECT:
                ctx.close();
                break;
            default:
                break;
        }
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
    }

    private void handlePubRel(ChannelHandlerContext ctx, MqttMessage message) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message.variableHeader();
        ctx.writeAndFlush(messageWithId(MqttMessageType.PUBCOMP, variableHeader.messageId()));
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
        return MqttMessageFactory.newMessage(
            new MqttFixedHeader(messageType, false, MqttQoS.AT_MOST_ONCE, false, 0),
            MqttMessageIdVariableHeader.from(packetId),
            null
        );
    }
}

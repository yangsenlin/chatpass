package com.chatpass.platform.mqtt.websocket;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.chatpass.platform.mqtt.handler.MqttBrokerChannelHandler;
import com.chatpass.platform.mqtt.netty.NettyServerListener;
import com.chatpass.platform.mqtt.netty.NettyTcpServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "chatpass.mqtt", name = "websocket-enabled", havingValue = "true", matchIfMissing = true)
public class MqttWebSocketBrokerServer extends NettyTcpServer {

    private static final Logger log = LoggerFactory.getLogger(MqttWebSocketBrokerServer.class);

    private final MqttBrokerProperties properties;
    private final MqttBrokerChannelHandler brokerChannelHandler;

    public MqttWebSocketBrokerServer(MqttBrokerProperties properties, MqttBrokerChannelHandler brokerChannelHandler) {
        super(properties.getWebsocketPort(), properties.getHost());
        this.properties = properties;
        this.brokerChannelHandler = brokerChannelHandler;
    }

    @PostConstruct
    public void start() {
        init();
        super.start(listener("ChatPass MQTT WebSocket broker"));
    }

    @PreDestroy
    public void stop() {
        super.stop(listener("ChatPass MQTT WebSocket broker"));
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline
            .addLast("idleHandler", new IdleStateHandler(0, 0, 60))
            .addLast("httpCodec", new HttpServerCodec())
            .addLast("httpAggregator", new HttpObjectAggregator(properties.getMaxPayloadBytes()))
            .addLast("webSocketProtocol", new WebSocketServerProtocolHandler(properties.getWebsocketPath(), "mqtt", true))
            .addLast("webSocketMqttFrameCodec", new WebSocketMqttFrameCodec())
            .addLast("mqttEncoder", MqttEncoder.INSTANCE)
            .addLast("mqttDecoder", new MqttDecoder(properties.getMaxPayloadBytes()))
            .addLast("mqttBrokerHandler", brokerChannelHandler);
    }

    @Override
    protected ChannelHandler getChannelHandler() {
        return brokerChannelHandler;
    }

    private NettyServerListener listener(String name) {
        return new NettyServerListener() {
            @Override
            public void onSuccess(int port) {
                log.info("{} lifecycle success on port {}", name, port);
            }

            @Override
            public void onFailure(Throwable throwable) {
                log.error("{} lifecycle failed", name, throwable);
            }
        };
    }
}

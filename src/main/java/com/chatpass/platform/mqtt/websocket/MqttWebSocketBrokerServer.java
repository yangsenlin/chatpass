package com.chatpass.platform.mqtt.websocket;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.chatpass.platform.mqtt.handler.MqttBrokerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "chatpass.mqtt", name = "websocket-enabled", havingValue = "true", matchIfMissing = true)
public class MqttWebSocketBrokerServer {

    private static final Logger log = LoggerFactory.getLogger(MqttWebSocketBrokerServer.class);

    private final MqttBrokerProperties properties;
    private final MqttBrokerChannelHandler brokerChannelHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public MqttWebSocketBrokerServer(MqttBrokerProperties properties, MqttBrokerChannelHandler brokerChannelHandler) {
        this.properties = properties;
        this.brokerChannelHandler = brokerChannelHandler;
    }

    @PostConstruct
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) {
                    channel.pipeline()
                        .addLast("httpCodec", new HttpServerCodec())
                        .addLast("httpAggregator", new HttpObjectAggregator(65536))
                        .addLast("webSocketProtocol", new WebSocketServerProtocolHandler(properties.getWebsocketPath(), "mqtt", true))
                        .addLast("webSocketMqttFrameCodec", new WebSocketMqttFrameCodec())
                        .addLast("mqttDecoder", new MqttDecoder())
                        .addLast("mqttEncoder", MqttEncoder.INSTANCE)
                        .addLast("mqttBrokerHandler", brokerChannelHandler);
                }
            });
        serverChannel = bootstrap.bind(properties.getHost(), properties.getWebsocketPort()).sync().channel();
        log.info(
            "ChatPass MQTT WebSocket broker started at {}:{}{}",
            properties.getHost(),
            properties.getWebsocketPort(),
            properties.getWebsocketPath()
        );
    }

    @PreDestroy
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("ChatPass MQTT WebSocket broker stopped");
    }
}

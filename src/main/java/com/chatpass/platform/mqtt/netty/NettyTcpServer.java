package com.chatpass.platform.mqtt.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public abstract class NettyTcpServer {

    public enum State {
        CREATED,
        INITIALIZED,
        STARTING,
        STARTED,
        SHUTDOWN
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicReference<State> serverState = new AtomicReference<>(State.CREATED);
    private final int port;
    private final String host;
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;

    protected NettyTcpServer(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void init() {
        if (!serverState.compareAndSet(State.CREATED, State.INITIALIZED)) {
            throw new NettyServerException("Server already initialized");
        }
    }

    public boolean isRunning() {
        return serverState.get() == State.STARTED;
    }

    public void start(NettyServerListener listener) {
        if (!serverState.compareAndSet(State.INITIALIZED, State.STARTING)) {
            throw new NettyServerException("Server already started or not initialized");
        }
        EventLoopGroup boss = getBossGroup();
        EventLoopGroup worker = getWorkerGroup();
        if (boss == null) {
            boss = new NioEventLoopGroup(getBossThreadNum(), getBossThreadFactory(), getSelectorProvider());
        }
        if (worker == null) {
            worker = new NioEventLoopGroup(getWorkerThreadNum(), getWorkerThreadFactory(), getSelectorProvider());
        }
        createServer(listener, boss, worker, getChannelFactory());
    }

    public void stop(NettyServerListener listener) {
        if (!serverState.compareAndSet(State.STARTED, State.SHUTDOWN)) {
            if (listener != null) {
                listener.onFailure(new NettyServerException("Server is not started or already shutdown"));
            }
            return;
        }
        log.info("Try shutdown {}...", getClass().getSimpleName());
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (listener != null) {
            listener.onSuccess(port);
        }
    }

    protected void initOptions(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(getChannelHandler());
    }

    protected abstract ChannelHandler getChannelHandler();

    protected int getBossThreadNum() {
        return 1;
    }

    protected int getWorkerThreadNum() {
        return 0;
    }

    protected String getBossThreadName() {
        return "mqtt-boss";
    }

    protected String getWorkerThreadName() {
        return "mqtt-worker";
    }

    protected ThreadFactory getBossThreadFactory() {
        return new DefaultThreadFactory(getBossThreadName());
    }

    protected ThreadFactory getWorkerThreadFactory() {
        return new DefaultThreadFactory(getWorkerThreadName());
    }

    protected EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    protected EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    protected ChannelFactory<? extends ServerChannel> getChannelFactory() {
        return NioServerSocketChannel::new;
    }

    protected SelectorProvider getSelectorProvider() {
        return SelectorProvider.provider();
    }

    private void createServer(
        NettyServerListener listener,
        EventLoopGroup boss,
        EventLoopGroup worker,
        ChannelFactory<? extends ServerChannel> channelFactory
    ) {
        this.bossGroup = boss;
        this.workerGroup = worker;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channelFactory(channelFactory);
            bootstrap.childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(io.netty.channel.Channel channel) {
                    initPipeline(channel.pipeline());
                }
            });
            initOptions(bootstrap);
            InetSocketAddress address = host == null || host.isBlank()
                ? new InetSocketAddress(port)
                : new InetSocketAddress(host, port);
            bootstrap.bind(address).addListener(future -> {
                if (future.isSuccess()) {
                    serverState.set(State.STARTED);
                    log.info("{} started on {}:{}", getClass().getSimpleName(), host, port);
                    if (listener != null) {
                        listener.onSuccess(port);
                    }
                } else {
                    log.error("{} start failed on port {}", getClass().getSimpleName(), port, future.cause());
                    if (listener != null) {
                        listener.onFailure(future.cause());
                    }
                }
            });
        } catch (RuntimeException ex) {
            if (listener != null) {
                listener.onFailure(ex);
            }
            throw new NettyServerException("Server start exception, port=" + port, ex);
        }
    }
}

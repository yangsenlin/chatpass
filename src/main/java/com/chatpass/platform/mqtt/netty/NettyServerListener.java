package com.chatpass.platform.mqtt.netty;

public interface NettyServerListener {

    void onSuccess(int port);

    void onFailure(Throwable throwable);
}

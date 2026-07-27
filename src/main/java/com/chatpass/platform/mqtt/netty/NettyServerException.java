package com.chatpass.platform.mqtt.netty;

public class NettyServerException extends RuntimeException {

    public NettyServerException(String message) {
        super(message);
    }

    public NettyServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.chatpass.platform.stream;

public enum StreamEventType {
    START,
    CHUNK,
    DONE,
    ERROR,
    CANCELLED,
    HEARTBEAT
}

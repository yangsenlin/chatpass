package com.chatpass.platform.protection;

public class MessageTooLargeException extends RuntimeException {

    public MessageTooLargeException(String message) {
        super(message);
    }
}

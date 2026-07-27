package com.chatpass.platform.buffer;

import com.chatpass.platform.message.MessageProcessingResult;
import com.chatpass.platform.message.UnifiedMessage;

import java.util.Optional;

public interface MessageBuffer {

    BufferedMessage enqueue(UnifiedMessage message);

    Optional<MessageProcessingResult> result(String bufferId);

    int size();
}

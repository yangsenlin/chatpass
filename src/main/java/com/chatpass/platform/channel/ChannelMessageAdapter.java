package com.chatpass.platform.channel;

import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;

import java.util.Map;

public interface ChannelMessageAdapter {

    boolean supports(ChannelType channel);

    UnifiedMessage normalize(ChannelType channel, Map<String, Object> payload);
}

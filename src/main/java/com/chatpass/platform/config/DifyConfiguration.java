package com.chatpass.platform.config;

import io.github.chatpass.dify.DifyApiFactory;
import io.github.chatpass.dify.api.DifyChatApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DifyConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "chatpass.dify", name = "enabled", havingValue = "true")
    public DifyChatApi difyChatApi(DifyProperties properties) {
        return DifyApiFactory.newInstance(properties.getBaseUrl(), properties.getApiKey()).newDifyChatApi();
    }
}

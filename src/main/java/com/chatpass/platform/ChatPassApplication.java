package com.chatpass.platform;

import com.chatpass.platform.config.ChatPassRoutingProperties;
import com.chatpass.platform.config.DifyProperties;
import com.chatpass.platform.history.HistoryElasticsearchProperties;
import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.chatpass.platform.security.IngressSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    ChatPassRoutingProperties.class,
    DifyProperties.class,
    MqttBrokerProperties.class,
    IngressSecurityProperties.class,
    HistoryElasticsearchProperties.class
})
public class ChatPassApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatPassApplication.class, args);
    }
}

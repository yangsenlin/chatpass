package com.chatpass.platform;

import com.chatpass.platform.config.ChatPassRoutingProperties;
import com.chatpass.platform.config.DifyProperties;
import com.chatpass.platform.mqtt.MqttBrokerProperties;
import com.chatpass.platform.security.IngressSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    ChatPassRoutingProperties.class,
    DifyProperties.class,
    MqttBrokerProperties.class,
    IngressSecurityProperties.class
})
public class ChatPassApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatPassApplication.class, args);
    }
}

package com.chatpass.config;

import com.chatpass.websocket.WebSocketEventHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置
 * 
 * Zulip 实时消息推送基于 STOMP 协议
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 消息代理前缀
        // /topic - 广播消息（如 Stream 消息）
        // /queue - 用户专属消息（如私信）
        config.enableSimpleBroker("/topic", "/queue");
        
        // 客户端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
        
        // 用户专属消息前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // 支持SockJS fallback
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 可以在这里添加认证拦截器
        // registration.interceptors(new WebSocketAuthInterceptor());
    }
}
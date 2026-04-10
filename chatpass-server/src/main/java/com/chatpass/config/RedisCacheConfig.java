package com.chatpass.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * 缓存名称与过期时间配置
     */
    public static final String CACHE_USERS = "users";
    public static final String CACHE_MESSAGES = "messages";
    public static final String CACHE_STREAMS = "streams";
    public static final String CACHE_REACTIONS = "reactions";
    public static final String CACHE_PRESENCE = "presence";
    public static final String CACHE_SETTINGS = "settings";

    // 缓存过期时间（秒）
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final Duration USERS_TTL = Duration.ofHours(2);
    private static final Duration MESSAGES_TTL = Duration.ofMinutes(10);
    private static final Duration STREAMS_TTL = Duration.ofHours(1);
    private static final Duration PRESENCE_TTL = Duration.ofMinutes(5);
    private static final Duration SETTINGS_TTL = Duration.ofHours(24);

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value 序列化（JSON）
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(DEFAULT_TTL)
                .disableCachingNullValues();

        // 不同缓存的个性化配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CACHE_USERS, defaultConfig.entryTtl(USERS_TTL));
        cacheConfigurations.put(CACHE_MESSAGES, defaultConfig.entryTtl(MESSAGES_TTL));
        cacheConfigurations.put(CACHE_STREAMS, defaultConfig.entryTtl(STREAMS_TTL));
        cacheConfigurations.put(CACHE_REACTIONS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(CACHE_PRESENCE, defaultConfig.entryTtl(PRESENCE_TTL));
        cacheConfigurations.put(CACHE_SETTINGS, defaultConfig.entryTtl(SETTINGS_TTL));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
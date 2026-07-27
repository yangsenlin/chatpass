package com.chatpass.platform.protection;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRateLimiter {

    private static final String KEY_PREFIX = "chatpass:rate-limit:";

    private final ChatPassProtectionProperties properties;
    private final StringRedisTemplate redisTemplate;

    public RedisRateLimiter(ChatPassProtectionProperties properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    public boolean allow(String key, long limit, Duration window) {
        if (!properties.isRateLimitEnabled() || limit <= 0) {
            return true;
        }
        String redisKey = KEY_PREFIX + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, window);
        }
        return count == null || count <= limit;
    }
}

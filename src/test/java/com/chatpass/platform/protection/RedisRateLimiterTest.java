package com.chatpass.platform.protection;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisRateLimiterTest {

    @Test
    void shouldAllowRequestsWithinLimitAndSetWindowTtl() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = valueOperations(redisTemplate);
        when(valueOperations.increment("chatpass:rate-limit:tenant-1")).thenReturn(1L);
        RedisRateLimiter limiter = new RedisRateLimiter(properties(true), redisTemplate);

        boolean allowed = limiter.allow("tenant-1", 10, Duration.ofMinutes(1));

        assertThat(allowed).isTrue();
        verify(redisTemplate).expire("chatpass:rate-limit:tenant-1", Duration.ofMinutes(1));
    }

    @Test
    void shouldRejectRequestsAboveLimit() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = valueOperations(redisTemplate);
        when(valueOperations.increment("chatpass:rate-limit:tenant-1")).thenReturn(11L);
        RedisRateLimiter limiter = new RedisRateLimiter(properties(true), redisTemplate);

        assertThat(limiter.allow("tenant-1", 10, Duration.ofMinutes(1))).isFalse();
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOperations(StringRedisTemplate redisTemplate) {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return valueOperations;
    }

    private ChatPassProtectionProperties properties(boolean enabled) {
        ChatPassProtectionProperties properties = new ChatPassProtectionProperties();
        properties.setRateLimitEnabled(enabled);
        return properties;
    }
}

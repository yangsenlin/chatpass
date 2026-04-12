package com.chatpass.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RateLimitService - API限流服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    // 默认限制配置
    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_WINDOW = 60; // seconds

    @Data
    public static class LimitStatus {
        private String clientId;
        private String endpoint;
        private Integer limit;
        private Integer remaining;
        private Long resetTime;
        private Boolean allowed;
    }

    /**
     * 检查是否允许请求
     */
    public boolean isAllowed(String clientId, String endpoint) {
        return isAllowed(clientId, endpoint, DEFAULT_LIMIT, DEFAULT_WINDOW);
    }

    /**
     * 检查是否允许请求（自定义限制）
     */
    public boolean isAllowed(String clientId, String endpoint, int limit, int windowSeconds) {
        String key = "rate_limit:" + clientId + ":" + endpoint;
        
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 0L;
        }
        
        if (count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        
        boolean allowed = count <= limit;
        
        if (!allowed) {
            log.warn("Rate limit exceeded for client {} on endpoint {}", clientId, endpoint);
        }
        
        return allowed;
    }

    /**
     * 获取限流状态
     */
    public LimitStatus getLimitStatus(String clientId, String endpoint) {
        return getLimitStatus(clientId, endpoint, DEFAULT_LIMIT, DEFAULT_WINDOW);
    }

    /**
     * 获取限流状态（自定义限制）
     */
    public LimitStatus getLimitStatus(String clientId, String endpoint, int limit, int windowSeconds) {
        String key = "rate_limit:" + clientId + ":" + endpoint;
        
        String countStr = redisTemplate.opsForValue().get(key);
        Long count = countStr != null ? Long.parseLong(countStr) : 0L;
        
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        
        LimitStatus status = new LimitStatus();
        status.setClientId(clientId);
        status.setEndpoint(endpoint);
        status.setLimit(limit);
        status.setRemaining(Math.max(0, limit - count.intValue()));
        status.setResetTime(ttl != null && ttl > 0 ? System.currentTimeMillis() + ttl * 1000 : 0L);
        status.setAllowed(count < limit);
        
        return status;
    }

    /**
     * 重置限流计数
     */
    public void resetLimit(String clientId, String endpoint) {
        String key = "rate_limit:" + clientId + ":" + endpoint;
        redisTemplate.delete(key);
        
        log.info("Reset rate limit for client {} on endpoint {}", clientId, endpoint);
    }

    /**
     * 消息发送限制（20/min）
     */
    public boolean checkMessageLimit(Long userId) {
        return isAllowed("user:" + userId, "message", 20, 60);
    }

    /**
     * 登录限制（5/min）
     */
    public boolean checkLoginLimit(String ip) {
        return isAllowed("ip:" + ip, "login", 5, 60);
    }

    /**
     * API通用限制（100/min）
     */
    public boolean checkApiLimit(Long userId) {
        return isAllowed("user:" + userId, "api", 100, 60);
    }
}
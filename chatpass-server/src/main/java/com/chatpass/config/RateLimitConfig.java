package com.chatpass.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limit 配置
 * 
 * API 请求频率限制
 */
@Configuration
public class RateLimitConfig {

    // 不同 API 的限流策略
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * 消息发送限流
     * - 每分钟最多 30 条消息
     */
    @Bean
    public Bucket messageRateLimit() {
        Bandwidth limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        buckets.put("message", bucket);
        return bucket;
    }

    /**
     * 登录限流
     * - 每分钟最多 5 次登录尝试
     */
    @Bean
    public Bucket loginRateLimit() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        buckets.put("login", bucket);
        return bucket;
    }

    /**
     * API 通用限流
     * - 分钟最多 100 次请求
     */
    @Bean
    public Bucket apiRateLimit() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        buckets.put("api", bucket);
        return bucket;
    }

    /**
     * 文件上传限流
     * - 每小时最多 20 次上传
     */
    @Bean
    public Bucket uploadRateLimit() {
        Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1)));
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        buckets.put("upload", bucket);
        return bucket;
    }

    /**
     * 获取指定类型的限流 Bucket
     */
    public Bucket getBucket(String type) {
        return buckets.get(type);
    }

    /**
     * 为特定用户创建限流 Bucket
     */
    public Bucket createUserBucket(Long userId, String type) {
        String key = type + ":" + userId;
        
        return buckets.computeIfAbsent(key, k -> {
            switch (type) {
                case "message":
                    Bandwidth limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
                    return Bucket.builder().addLimit(limit).build();
                case "login":
                    Bandwidth loginLimit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
                    return Bucket.builder().addLimit(loginLimit).build();
                default:
                    Bandwidth defaultLimit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
                    return Bucket.builder().addLimit(defaultLimit).build();
            }
        });
    }
}
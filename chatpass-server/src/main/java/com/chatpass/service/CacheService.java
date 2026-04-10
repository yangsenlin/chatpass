package com.chatpass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 
 * 提供统一的缓存操作接口
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Key 前缀
    private static final String PREFIX_USER = "user:";
    private static final String PREFIX_MESSAGE = "message:";
    private static final String PREFIX_STREAM = "stream:";
    private static final String PREFIX_ONLINE = "online:";
    private static final String PREFIX_TYPING = "typing:";
    private static final String PREFIX_SETTINGS = "settings:";

    /**
     * 缓存用户信息
     */
    public void cacheUser(Long userId, Object user) {
        String key = PREFIX_USER + userId;
        redisTemplate.opsForValue().set(key, user, Duration.ofHours(2));
        log.debug("Cached user: {}", userId);
    }

    /**
     * 获取缓存用户
     */
    public Optional<Object> getCachedUser(Long userId) {
        String key = PREFIX_USER + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }

    /**
     * 删除用户缓存
     */
    public void evictUser(Long userId) {
        String key = PREFIX_USER + userId;
        redisTemplate.delete(key);
        log.debug("Evicted user cache: {}", userId);
    }

    /**
     * 缓存消息
     */
    public void cacheMessage(Long messageId, Object message) {
        String key = PREFIX_MESSAGE + messageId;
        redisTemplate.opsForValue().set(key, message, Duration.ofMinutes(10));
        log.debug("Cached message: {}", messageId);
    }

    /**
     * 获取缓存消息
     */
    public Optional<Object> getCachedMessage(Long messageId) {
        String key = PREFIX_MESSAGE + messageId;
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }

    /**
     * 批量获取缓存消息
     */
    public Map<Long, Object> getCachedMessages(List<Long> messageIds) {
        Map<Long, Object> result = new HashMap<>();
        for (Long id : messageIds) {
            getCachedMessage(id).ifPresent(msg -> result.put(id, msg));
        }
        return result;
    }

    /**
     * 删除消息缓存
     */
    public void evictMessage(Long messageId) {
        String key = PREFIX_MESSAGE + messageId;
        redisTemplate.delete(key);
        log.debug("Evicted message cache: {}", messageId);
    }

    /**
     * 缓存 Stream 信息
     */
    public void cacheStream(Long streamId, Object stream) {
        String key = PREFIX_STREAM + streamId;
        redisTemplate.opsForValue().set(key, stream, Duration.ofHours(1));
        log.debug("Cached stream: {}", streamId);
    }

    /**
     * 获取缓存 Stream
     */
    public Optional<Object> getCachedStream(Long streamId) {
        String key = PREFIX_STREAM + streamId;
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }

    /**
     * 删除 Stream 缓存
     */
    public void evictStream(Long streamId) {
        String key = PREFIX_STREAM + streamId;
        redisTemplate.delete(key);
        log.debug("Evicted stream cache: {}", streamId);
    }

    /**
     * 记录用户在线状态
     */
    public void setOnline(Long userId, String status) {
        String key = PREFIX_ONLINE + userId;
        redisTemplate.opsForValue().set(key, status, Duration.ofMinutes(5));
        log.debug("Set user {} online status: {}", userId, status);
    }

    /**
     * 获取用户在线状态
     */
    public String getOnlineStatus(Long userId) {
        String key = PREFIX_ONLINE + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : "offline";
    }

    /**
     * 获取所有在线用户
     */
    public Set<Long> getOnlineUsers() {
        Set<String> keys = redisTemplate.keys(PREFIX_ONLINE + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<Long> onlineUsers = new HashSet<>();
        for (String key : keys) {
            String status = (String) redisTemplate.opsForValue().get(key);
            if ("active".equals(status)) {
                Long userId = Long.parseLong(key.substring(PREFIX_ONLINE.length()));
                onlineUsers.add(userId);
            }
        }
        return onlineUsers;
    }

    /**
     * 记录输入状态
     */
    public void setTyping(Long userId, Long recipientId, String topic) {
        String key = PREFIX_TYPING + recipientId + ":" + (topic != null ? topic : "dm");
        redisTemplate.opsForSet().add(key, userId.toString());
        redisTemplate.expire(key, Duration.ofSeconds(15));
        log.debug("User {} typing in recipient {} topic {}", userId, recipientId, topic);
    }

    /**
     * 获取输入中的用户
     */
    public Set<String> getTypingUsers(Long recipientId, String topic) {
        String key = PREFIX_TYPING + recipientId + ":" + (topic != null ? topic : "dm");
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        for (Object obj : members) {
            if (obj != null) {
                result.add(obj.toString());
            }
        }
        return result;
    }

    /**
     * 清除输入状态
     */
    public void clearTyping(Long userId, Long recipientId, String topic) {
        String key = PREFIX_TYPING + recipientId + ":" + (topic != null ? topic : "dm");
        redisTemplate.opsForSet().remove(key, userId.toString());
    }

    /**
     * 缓存用户设置
     */
    public void cacheSettings(Long userId, Object settings) {
        String key = PREFIX_SETTINGS + userId;
        redisTemplate.opsForValue().set(key, settings, Duration.ofHours(24));
        log.debug("Cached settings for user: {}", userId);
    }

    /**
     * 获取缓存设置
     */
    public Optional<Object> getCachedSettings(Long userId) {
        String key = PREFIX_SETTINGS + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }

    /**
     * 清除所有缓存
     */
    public void clearAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared {} cache keys", keys.size());
        }
    }
}
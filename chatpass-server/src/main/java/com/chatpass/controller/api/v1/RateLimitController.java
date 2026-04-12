package com.chatpass.controller.api.v1;

import com.chatpass.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * RateLimitController - Rate Limit API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitService rateLimitService;

    /**
     * 获取限流状态
     */
    @GetMapping("/rate_limit/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(
            @RequestParam String client_id,
            @RequestParam(required = false) String endpoint) {

        if (endpoint == null) {
            endpoint = "api";
        }

        RateLimitService.LimitStatus status = rateLimitService.getLimitStatus(client_id, endpoint);

        return ResponseEntity.ok(Map.of(
                "client_id", status.getClientId(),
                "endpoint", status.getEndpoint(),
                "limit", status.getLimit(),
                "remaining", status.getRemaining(),
                "reset_time", status.getResetTime(),
                "allowed", status.getAllowed()
        ));
    }

    /**
     * 重置限流计数
     */
    @PostMapping("/rate_limit/reset")
    public ResponseEntity<Map<String, Object>> resetRateLimit(
            @RequestParam String client_id,
            @RequestParam String endpoint) {

        rateLimitService.resetLimit(client_id, endpoint);

        return ResponseEntity.ok(Map.of(
                "client_id", client_id,
                "endpoint", endpoint,
                "result", "success"
        ));
    }

    /**
     * 检查消息发送限制
     */
    @GetMapping("/rate_limit/message/{user_id}")
    public ResponseEntity<Map<String, Object>> checkMessageLimit(
            @PathVariable("user_id") Long userId) {

        boolean allowed = rateLimitService.checkMessageLimit(userId);
        RateLimitService.LimitStatus status = rateLimitService.getLimitStatus("user:" + userId, "message", 20, 60);

        return ResponseEntity.ok(Map.of(
                "user_id", userId,
                "allowed", allowed,
                "limit", status.getLimit(),
                "remaining", status.getRemaining()
        ));
    }

    /**
     * 检查登录限制
     */
    @GetMapping("/rate_limit/login")
    public ResponseEntity<Map<String, Object>> checkLoginLimit(
            @RequestParam String ip) {

        boolean allowed = rateLimitService.checkLoginLimit(ip);
        RateLimitService.LimitStatus status = rateLimitService.getLimitStatus("ip:" + ip, "login", 5, 60);

        return ResponseEntity.ok(Map.of(
                "ip", ip,
                "allowed", allowed,
                "limit", status.getLimit(),
                "remaining", status.getRemaining()
        ));
    }
}
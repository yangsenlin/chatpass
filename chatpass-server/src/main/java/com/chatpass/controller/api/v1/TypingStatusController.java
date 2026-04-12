package com.chatpass.controller.api.v1;

import com.chatpass.dto.TypingStatusDTO;
import com.chatpass.service.TypingStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 输入状态控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TypingStatusController {
    
    private final TypingStatusService typingService;
    
    /**
     * 开始输入状态
     */
    @PostMapping("/users/{userId}/typing")
    public ResponseEntity<TypingStatusDTO> startTyping(
            @PathVariable Long userId,
            @RequestParam(required = false) Long streamId,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String toUserIds,
            @RequestParam(required = false) Long realmId) {
        
        TypingStatusDTO typing = typingService.startTyping(userId, streamId, topic, toUserIds, realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(typing);
    }
    
    /**
     * 停止输入状态
     */
    @DeleteMapping("/users/{userId}/typing")
    public ResponseEntity<Void> stopTyping(@PathVariable Long userId) {
        typingService.stopTyping(userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取用户的输入状态
     */
    @GetMapping("/users/{userId}/typing")
    public ResponseEntity<List<TypingStatusDTO>> getUserTypingStatus(@PathVariable Long userId) {
        List<TypingStatusDTO> typing = typingService.getUserTypingStatus(userId);
        return ResponseEntity.ok(typing);
    }
    
    /**
     * 获取话题正在输入的用户
     */
    @GetMapping("/streams/{streamId}/topics/{topic}/typing")
    public ResponseEntity<List<TypingStatusDTO>> getTypingUsers(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        
        List<TypingStatusDTO> typing = typingService.getTypingUsers(streamId, topic);
        return ResponseEntity.ok(typing);
    }
    
    /**
     * 获取频道正在输入的用户
     */
    @GetMapping("/streams/{streamId}/typing")
    public ResponseEntity<List<TypingStatusDTO>> getStreamTypingUsers(@PathVariable Long streamId) {
        List<TypingStatusDTO> typing = typingService.getStreamTypingUsers(streamId);
        return ResponseEntity.ok(typing);
    }
    
    /**
     * 统计正在输入的用户数
     */
    @GetMapping("/streams/{streamId}/topics/{topic}/typing/count")
    public ResponseEntity<Long> countTypingUsers(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        
        long count = typingService.countTypingUsers(streamId, topic);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 清理过期输入状态
     */
    @PostMapping("/typing/cleanup")
    public ResponseEntity<Integer> cleanupExpiredTyping(
            @RequestParam(defaultValue = "30") int secondsThreshold) {
        
        typingService.cleanupExpiredTyping(secondsThreshold);
        return ResponseEntity.ok(0);
    }
}

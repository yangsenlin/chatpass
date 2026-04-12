package com.chatpass.controller.api.v1;

import com.chatpass.dto.MessageFlagDTO;
import com.chatpass.service.MessageFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息标记控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MessageFlagController {
    
    private final MessageFlagService flagService;
    
    /**
     * 标记消息
     */
    @PostMapping("/messages/{messageId}/flags")
    public ResponseEntity<MessageFlagDTO> flagMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "star") String flagType,
            @RequestParam(required = false) Long realmId) {
        
        MessageFlagDTO flag = flagService.flagMessage(userId, messageId, flagType, realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(flag);
    }
    
    /**
     * 取消标记
     */
    @DeleteMapping("/messages/{messageId}/flags")
    public ResponseEntity<Void> unflagMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        flagService.unflagMessage(userId, messageId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取用户标记列表
     */
    @GetMapping("/users/{userId}/flags")
    public ResponseEntity<List<MessageFlagDTO>> getUserFlags(@PathVariable Long userId) {
        List<MessageFlagDTO> flags = flagService.getUserFlags(userId);
        return ResponseEntity.ok(flags);
    }
    
    /**
     * 获取用户特定类型标记
     */
    @GetMapping("/users/{userId}/flags/type/{flagType}")
    public ResponseEntity<List<MessageFlagDTO>> getUserFlagsByType(
            @PathVariable Long userId,
            @PathVariable String flagType) {
        
        List<MessageFlagDTO> flags = flagService.getUserFlagsByType(userId, flagType);
        return ResponseEntity.ok(flags);
    }
    
    /**
     * 获取消息标记用户
     */
    @GetMapping("/messages/{messageId}/flags")
    public ResponseEntity<List<MessageFlagDTO>> getMessageFlags(@PathVariable Long messageId) {
        List<MessageFlagDTO> flags = flagService.getMessageFlags(messageId);
        return ResponseEntity.ok(flags);
    }
    
    /**
     * 检查是否已标记
     */
    @GetMapping("/messages/{messageId}/flags/check")
    public ResponseEntity<Boolean> isFlagged(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        boolean flagged = flagService.isFlagged(userId, messageId);
        return ResponseEntity.ok(flagged);
    }
    
    /**
     * 获取标记用户列表
     */
    @GetMapping("/messages/{messageId}/flags/users")
    public ResponseEntity<List<Long>> getFlagUsers(@PathVariable Long messageId) {
        List<Long> users = flagService.getFlagUsers(messageId);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 统计用户标记数
     */
    @GetMapping("/users/{userId}/flags/count")
    public ResponseEntity<Long> countUserFlags(@PathVariable Long userId) {
        long count = flagService.countUserFlags(userId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 统计消息标记数
     */
    @GetMapping("/messages/{messageId}/flags/count")
    public ResponseEntity<Long> countMessageFlags(@PathVariable Long messageId) {
        long count = flagService.countMessageFlags(messageId);
        return ResponseEntity.ok(count);
    }
}

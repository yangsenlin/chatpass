package com.chatpass.controller.api.v1;

import com.chatpass.dto.MessageReadStatusDTO;
import com.chatpass.service.MessageReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息阅读状态控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MessageReadStatusController {
    
    private final MessageReadStatusService readStatusService;
    
    /**
     * 标记消息已读
     */
    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<MessageReadStatusDTO.ReadStatus> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long realmId) {
        
        MessageReadStatusDTO.ReadStatus readStatus = readStatusService.markAsRead(userId, messageId, realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatus);
    }
    
    /**
     * 批量标记已读
     */
    @PostMapping("/users/{userId}/read/batch")
    public ResponseEntity<Void> batchMarkAsRead(
            @PathVariable Long userId,
            @RequestParam List<Long> messageIds,
            @RequestParam(required = false) Long realmId) {
        
        readStatusService.batchMarkAsRead(userId, messageIds, realmId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 取消已读标记
     */
    @DeleteMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markAsUnread(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        readStatusService.markAsUnread(userId, messageId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取用户已读消息列表
     */
    @GetMapping("/users/{userId}/read")
    public ResponseEntity<List<MessageReadStatusDTO.ReadStatus>> getUserReadMessages(@PathVariable Long userId) {
        List<MessageReadStatusDTO.ReadStatus> readMessages = readStatusService.getUserReadMessages(userId);
        return ResponseEntity.ok(readMessages);
    }
    
    /**
     * 获取消息已读用户列表
     */
    @GetMapping("/messages/{messageId}/readers")
    public ResponseEntity<List<MessageReadStatusDTO.ReadStatus>> getMessageReaders(@PathVariable Long messageId) {
        List<MessageReadStatusDTO.ReadStatus> readers = readStatusService.getMessageReaders(messageId);
        return ResponseEntity.ok(readers);
    }
    
    /**
     * 检查消息是否已读
     */
    @GetMapping("/messages/{messageId}/read/check")
    public ResponseEntity<Boolean> isRead(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        boolean isRead = readStatusService.isRead(userId, messageId);
        return ResponseEntity.ok(isRead);
    }
    
    /**
     * 获取已读用户列表
     */
    @GetMapping("/messages/{messageId}/read/users")
    public ResponseEntity<List<Long>> getReadUsers(@PathVariable Long messageId) {
        List<Long> users = readStatusService.getReadUsers(messageId);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 统计已读用户数
     */
    @GetMapping("/messages/{messageId}/read/count")
    public ResponseEntity<Long> countReaders(@PathVariable Long messageId) {
        long count = readStatusService.countReaders(messageId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 统计用户已读消息数
     */
    @GetMapping("/users/{userId}/read/count")
    public ResponseEntity<Long> countReadMessages(@PathVariable Long userId) {
        long count = readStatusService.countReadMessages(userId);
        return ResponseEntity.ok(count);
    }
}

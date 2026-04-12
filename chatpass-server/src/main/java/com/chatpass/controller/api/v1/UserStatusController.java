package com.chatpass.controller.api.v1;

import com.chatpass.dto.UserStatusDTO;
import com.chatpass.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户状态控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserStatusController {
    
    private final UserStatusService statusService;
    
    /**
     * 设置用户状态
     */
    @PostMapping("/users/{userId}/status")
    public ResponseEntity<UserStatusDTO> setUserStatus(
            @PathVariable Long userId,
            @RequestParam(required = false) String statusText,
            @RequestParam(required = false) String statusEmoji,
            @RequestParam(required = false) Integer durationSeconds,
            @RequestParam(required = false) Long realmId) {
        
        UserStatusDTO status = statusService.setUserStatus(userId, statusText, statusEmoji, durationSeconds, realmId);
        return ResponseEntity.ok(status);
    }
    
    /**
     * 清除用户状态
     */
    @DeleteMapping("/users/{userId}/status")
    public ResponseEntity<Void> clearUserStatus(@PathVariable Long userId) {
        statusService.clearUserStatus(userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取用户状态
     */
    @GetMapping("/users/{userId}/status")
    public ResponseEntity<UserStatusDTO> getUserStatus(@PathVariable Long userId) {
        return statusService.getUserStatus(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取组织的用户状态
     */
    @GetMapping("/realm/{realmId}/user_statuses")
    public ResponseEntity<List<UserStatusDTO>> getRealmUserStatuses(@PathVariable Long realmId) {
        List<UserStatusDTO> statuses = statusService.getRealmUserStatuses(realmId);
        return ResponseEntity.ok(statuses);
    }
    
    /**
     * 获取所有有效状态
     */
    @GetMapping("/user_statuses")
    public ResponseEntity<List<UserStatusDTO>> getActiveStatuses() {
        List<UserStatusDTO> statuses = statusService.getActiveStatuses();
        return ResponseEntity.ok(statuses);
    }
    
    /**
     * 清理过期状态
     */
    @PostMapping("/user_statuses/cleanup")
    public ResponseEntity<Integer> cleanupExpiredStatuses() {
        statusService.cleanupExpiredStatuses();
        return ResponseEntity.ok(0);
    }
}

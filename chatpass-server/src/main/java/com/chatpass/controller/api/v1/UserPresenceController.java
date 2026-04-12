package com.chatpass.controller.api.v1;

import com.chatpass.dto.UserPresenceDTO;
import com.chatpass.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户在线状态控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserPresenceController {
    
    private final UserPresenceService presenceService;
    
    /**
     * 设置在线状态
     */
    @PostMapping("/users/{userId}/presence")
    public ResponseEntity<UserPresenceDTO> setPresence(
            @PathVariable Long userId,
            @RequestParam String status,
            @RequestParam(required = false) String statusMessage,
            @RequestParam(required = false) Long realmId) {
        
        UserPresenceDTO presence = presenceService.setPresence(userId, status, statusMessage, realmId);
        return ResponseEntity.ok(presence);
    }
    
    /**
     * 用户上线
     */
    @PostMapping("/users/{userId}/presence/online")
    public ResponseEntity<UserPresenceDTO> markOnline(
            @PathVariable Long userId,
            @RequestParam(required = false) Long realmId) {
        
        UserPresenceDTO presence = presenceService.markOnline(userId, realmId);
        return ResponseEntity.ok(presence);
    }
    
    /**
     * 用户离线
     */
    @PostMapping("/users/{userId}/presence/offline")
    public ResponseEntity<UserPresenceDTO> markOffline(
            @PathVariable Long userId,
            @RequestParam(required = false) Long realmId) {
        
        UserPresenceDTO presence = presenceService.markOffline(userId, realmId);
        return ResponseEntity.ok(presence);
    }
    
    /**
     * 设置空闲状态
     */
    @PostMapping("/users/{userId}/presence/idle")
    public ResponseEntity<UserPresenceDTO> markIdle(
            @PathVariable Long userId,
            @RequestParam(required = false) Long realmId) {
        
        UserPresenceDTO presence = presenceService.markIdle(userId, realmId);
        return ResponseEntity.ok(presence);
    }
    
    /**
     * 设置忙碌状态
     */
    @PostMapping("/users/{userId}/presence/busy")
    public ResponseEntity<UserPresenceDTO> markBusy(
            @PathVariable Long userId,
            @RequestParam(required = false) String statusMessage,
            @RequestParam(required = false) Long realmId) {
        
        UserPresenceDTO presence = presenceService.markBusy(userId, statusMessage, realmId);
        return ResponseEntity.ok(presence);
    }
    
    /**
     * 获取用户状态
     */
    @GetMapping("/users/{userId}/presence")
    public ResponseEntity<UserPresenceDTO> getUserPresence(@PathVariable Long userId) {
        return presenceService.getUserPresence(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取组织的用户状态
     */
    @GetMapping("/realm/{realmId}/presences")
    public ResponseEntity<List<UserPresenceDTO>> getRealmPresences(@PathVariable Long realmId) {
        List<UserPresenceDTO> presences = presenceService.getRealmPresences(realmId);
        return ResponseEntity.ok(presences);
    }
    
    /**
     * 获取在线用户
     */
    @GetMapping("/realm/{realmId}/presences/online")
    public ResponseEntity<List<UserPresenceDTO>> getOnlineUsers(@PathVariable Long realmId) {
        List<UserPresenceDTO> users = presenceService.getOnlineUsers(realmId);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 获取活跃用户
     */
    @GetMapping("/realm/{realmId}/presences/active")
    public ResponseEntity<List<UserPresenceDTO>> getActiveUsers(
            @PathVariable Long realmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        List<UserPresenceDTO> users = presenceService.getActiveUsers(realmId, since);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 统计在线用户数量
     */
    @GetMapping("/realm/{realmId}/presences/online_count")
    public ResponseEntity<Long> countOnlineUsers(@PathVariable Long realmId) {
        long count = presenceService.countOnlineUsers(realmId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 设置状态消息
     */
    @PatchMapping("/users/{userId}/presence/message")
    public ResponseEntity<UserPresenceDTO> setStatusMessage(
            @PathVariable Long userId,
            @RequestParam String statusMessage) {
        
        UserPresenceDTO presence = presenceService.setStatusMessage(userId, statusMessage);
        return ResponseEntity.ok(presence);
    }
    
    /**
     * 设置推送通知
     */
    @PatchMapping("/users/{userId}/presence/push")
    public ResponseEntity<Void> setPushNotifications(
            @PathVariable Long userId,
            @RequestParam boolean enabled) {
        
        presenceService.setPushNotifications(userId, enabled);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 设置显示离线状态
     */
    @PatchMapping("/users/{userId}/presence/show_offline")
    public ResponseEntity<Void> setShowOffline(
            @PathVariable Long userId,
            @RequestParam boolean showOffline) {
        
        presenceService.setShowOffline(userId, showOffline);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 更新活跃时间
     */
    @PostMapping("/users/{userId}/presence/activity")
    public ResponseEntity<Void> updateActivity(@PathVariable Long userId) {
        presenceService.updateActivity(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 清理不活跃用户
     */
    @PostMapping("/presences/cleanup")
    public ResponseEntity<Integer> cleanupInactiveUsers(
            @RequestParam(defaultValue = "30") int minutesThreshold) {
        
        presenceService.cleanupInactiveUsers(minutesThreshold);
        return ResponseEntity.ok(0);
    }
}

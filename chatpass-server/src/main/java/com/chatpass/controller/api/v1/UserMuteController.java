package com.chatpass.controller.api.v1;

import com.chatpass.dto.UserMuteDTO;
import com.chatpass.service.UserMuteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户静音控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserMuteController {
    
    private final UserMuteService muteService;
    
    /**
     * 静音用户
     */
    @PostMapping("/users/{userId}/mutes")
    public ResponseEntity<UserMuteDTO> muteUser(
            @PathVariable Long userId,
            @RequestParam Long mutedUserId,
            @RequestParam(required = false) Long realmId) {
        
        UserMuteDTO mute = muteService.muteUser(userId, mutedUserId, realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mute);
    }
    
    /**
     * 取消静音
     */
    @DeleteMapping("/users/{userId}/mutes/{mutedUserId}")
    public ResponseEntity<Void> unmuteUser(
            @PathVariable Long userId,
            @PathVariable Long mutedUserId) {
        
        muteService.unmuteUser(userId, mutedUserId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取用户静音列表
     */
    @GetMapping("/users/{userId}/mutes")
    public ResponseEntity<List<UserMuteDTO>> getUserMutes(@PathVariable Long userId) {
        List<UserMuteDTO> mutes = muteService.getUserMutes(userId);
        return ResponseEntity.ok(mutes);
    }
    
    /**
     * 检查是否静音
     */
    @GetMapping("/users/{userId}/mutes/{mutedUserId}/check")
    public ResponseEntity<Boolean> isMuted(
            @PathVariable Long userId,
            @PathVariable Long mutedUserId) {
        
        boolean muted = muteService.isMuted(userId, mutedUserId);
        return ResponseEntity.ok(muted);
    }
    
    /**
     * 统计静音数量
     */
    @GetMapping("/users/{userId}/mutes/count")
    public ResponseEntity<Long> countMutes(@PathVariable Long userId) {
        long count = muteService.countMutes(userId);
        return ResponseEntity.ok(count);
    }
}

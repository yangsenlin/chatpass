package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.UserDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户设置控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Settings", description = "用户设置 API")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;
    private final SecurityUtil securityUtil;

    @GetMapping("/users/me/settings")
    @Operation(summary = "获取用户设置")
    public ResponseEntity<ApiResponse<UserDTO.ProfileResponse>> getSettings() {
        Long userId = securityUtil.getCurrentUserId();
        UserDTO.ProfileResponse response = userSettingsService.getSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/users/me/settings")
    @Operation(summary = "更新用户设置")
    public ResponseEntity<ApiResponse<UserDTO.ProfileResponse>> updateSettings(
            @RequestBody UserDTO.UpdateRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        UserDTO.ProfileResponse response = userSettingsService.updateSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/users/me/notifications")
    @Operation(summary = "更新通知设置")
    public ResponseEntity<ApiResponse<Void>> updateNotificationSettings(
            @RequestParam(required = false) Boolean enableDesktopNotifications,
            @RequestParam(required = false) Boolean enableSounds,
            @RequestParam(required = false) Boolean enableOfflineEmailNotifications,
            @RequestParam(required = false) Boolean enableOfflinePushNotifications) {
        
        Long userId = securityUtil.getCurrentUserId();
        userSettingsService.updateNotificationSettings(
                userId, enableDesktopNotifications, enableSounds,
                enableOfflineEmailNotifications, enableOfflinePushNotifications);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/users/me/timezone")
    @Operation(summary = "设置时区")
    public ResponseEntity<ApiResponse<Void>> setTimezone(@RequestParam String timezone) {
        Long userId = securityUtil.getCurrentUserId();
        userSettingsService.setTimezone(userId, timezone);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/users/me/language")
    @Operation(summary = "设置语言")
    public ResponseEntity<ApiResponse<Void>> setLanguage(@RequestParam String language) {
        Long userId = securityUtil.getCurrentUserId();
        userSettingsService.setLanguage(userId, language);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
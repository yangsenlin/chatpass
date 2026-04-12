package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.UserDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.UserAvatarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户头像控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Avatar", description = "用户头像 API")
public class UserAvatarController {

    private final UserAvatarService avatarService;
    private final SecurityUtil securityUtil;

    @PostMapping("/users/me/avatar")
    @Operation(summary = "上传用户头像")
    public ResponseEntity<ApiResponse<UserDTO.AvatarResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        UserDTO.AvatarResponse response = avatarService.uploadAvatar(userId, realmId, file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/users/me/avatar")
    @Operation(summary = "删除用户头像")
    public ResponseEntity<ApiResponse<Void>> deleteAvatar() {
        Long userId = securityUtil.getCurrentUserId();
        avatarService.deleteAvatar(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/{userId}/avatar")
    @Operation(summary = "获取用户头像")
    public ResponseEntity<ApiResponse<UserDTO.AvatarResponse>> getUserAvatar(
            @PathVariable Long userId) {
        
        UserDTO.AvatarResponse response = avatarService.getAvatar(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
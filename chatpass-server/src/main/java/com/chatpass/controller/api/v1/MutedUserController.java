package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.MutedUserDTO;
import com.chatpass.entity.MutedUser;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.MutedUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MutedUser 控制器
 * 
 * 用户屏蔽功能 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Muted Users", description = "用户屏蔽 API")
public class MutedUserController {

    private final MutedUserService mutedUserService;
    private final SecurityUtil securityUtil;

    @PostMapping("/users/me/muted-users")
    @Operation(summary = "屏蔽用户")
    public ResponseEntity<ApiResponse<MutedUserDTO.Response>> muteUser(
            @RequestBody MutedUserDTO.MuteRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        MutedUserDTO.Response response = mutedUserService.muteUser(userId, request.getMutedUserId());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/muted-users")
    @Operation(summary = "获取屏蔽用户列表")
    public ResponseEntity<ApiResponse<MutedUserDTO.ListResponse>> getMutedUsers() {
        Long userId = securityUtil.getCurrentUserId();
        MutedUserDTO.ListResponse response = mutedUserService.getMutedUsers(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/muted-users/ids")
    @Operation(summary = "获取屏蔽用户ID列表（轻量级）")
    public ResponseEntity<ApiResponse<MutedUserDTO.IdListResponse>> getMutedUserIds() {
        Long userId = securityUtil.getCurrentUserId();
        List<Long> ids = mutedUserService.getMutedUserIds(userId);
        
        return ResponseEntity.ok(ApiResponse.success(MutedUserDTO.IdListResponse.builder()
                .mutedUserIds(ids)
                .count(ids.size())
                .build()));
    }

    @DeleteMapping("/users/me/muted-users/{mutedUserId}")
    @Operation(summary = "取消屏蔽用户")
    public ResponseEntity<ApiResponse<Void>> unmuteUser(@PathVariable Long mutedUserId) {
        Long userId = securityUtil.getCurrentUserId();
        mutedUserService.unmuteUser(userId, mutedUserId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me/muted-users/check/{targetUserId}")
    @Operation(summary = "检查是否已屏蔽某用户")
    public ResponseEntity<ApiResponse<Boolean>> checkMuted(@PathVariable Long targetUserId) {
        Long userId = securityUtil.getCurrentUserId();
        boolean isMuted = mutedUserService.isUserMuted(userId, targetUserId);
        
        return ResponseEntity.ok(ApiResponse.success(isMuted));
    }

    @DeleteMapping("/users/me/muted-users")
    @Operation(summary = "清除所有屏蔽")
    public ResponseEntity<ApiResponse<Void>> clearAllMutedUsers() {
        Long userId = securityUtil.getCurrentUserId();
        mutedUserService.clearAllMutedUsers(userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
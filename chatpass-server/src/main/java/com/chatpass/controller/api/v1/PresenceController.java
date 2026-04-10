package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.PresenceDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.PresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Presence 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Presence", description = "用户在线状态 API")
public class PresenceController {

    private final PresenceService presenceService;
    private final SecurityUtil securityUtil;

    @PostMapping("/users/me/presence")
    @Operation(summary = "更新用户状态")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@RequestBody PresenceDTO.UpdateRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        presenceService.updateStatus(userId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me/presence")
    @Operation(summary = "获取当前用户状态")
    public ResponseEntity<ApiResponse<PresenceDTO.Response>> getStatus() {
        Long userId = securityUtil.getCurrentUserId();
        String status = presenceService.getStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(
                PresenceDTO.Response.builder()
                        .userId(userId)
                        .status(status)
                        .build()));
    }

    @GetMapping("/users/{userId}/presence")
    @Operation(summary = "获取用户状态")
    public ResponseEntity<ApiResponse<PresenceDTO.Response>> getUserStatus(@PathVariable Long userId) {
        String status = presenceService.getStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(
                PresenceDTO.Response.builder()
                        .userId(userId)
                        .status(status)
                        .build()));
    }
}
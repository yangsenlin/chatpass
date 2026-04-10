package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.TypingDTO;
import com.chatpass.entity.TypingStatus;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.TypingStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TypingStatus 控制器
 * 
 * 输入提示状态 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Typing Status", description = "输入提示 API")
public class TypingStatusController {

    private final TypingStatusService typingService;
    private final SecurityUtil securityUtil;

    @PostMapping("/typing/direct/{recipientId}/start")
    @Operation(summary = "开始输入（私信）")
    public ResponseEntity<ApiResponse<TypingDTO.TypingResponse>> startTypingDirect(
            @PathVariable Long recipientId) {
        Long userId = securityUtil.getCurrentUserId();
        
        TypingStatus status = typingService.startTypingDirect(userId, recipientId);
        
        return ResponseEntity.ok(ApiResponse.success(typingService.toResponse(status)));
    }

    @PostMapping("/typing/direct/{recipientId}/stop")
    @Operation(summary = "停止输入（私信）")
    public ResponseEntity<ApiResponse<Void>> stopTypingDirect(
            @PathVariable Long recipientId) {
        Long userId = securityUtil.getCurrentUserId();
        
        typingService.stopTypingDirect(userId, recipientId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/typing/stream/{streamId}/start")
    @Operation(summary = "开始输入（频道）")
    public ResponseEntity<ApiResponse<TypingDTO.TypingResponse>> startTypingStream(
            @PathVariable Long streamId,
            @RequestBody TypingDTO.StreamTypingRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        TypingStatus status = typingService.startTypingStream(userId, streamId, request.getTopic());
        
        return ResponseEntity.ok(ApiResponse.success(typingService.toResponse(status)));
    }

    @PostMapping("/typing/stream/{streamId}/stop")
    @Operation(summary = "停止输入（频道）")
    public ResponseEntity<ApiResponse<Void>> stopTypingStream(
            @PathVariable Long streamId,
            @RequestBody TypingDTO.StreamTypingRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        typingService.stopTypingStream(userId, streamId, request.getTopic());
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/typing/stop")
    @Operation(summary = "停止所有输入")
    public ResponseEntity<ApiResponse<Void>> stopAllTyping() {
        Long userId = securityUtil.getCurrentUserId();
        
        typingService.stopTyping(userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/typing/direct/{recipientId}/keep")
    @Operation(summary = "保持输入状态")
    public ResponseEntity<ApiResponse<Void>> keepTyping(
            @PathVariable Long recipientId) {
        Long userId = securityUtil.getCurrentUserId();
        
        typingService.keepTyping(userId, recipientId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/typing/direct/{recipientId}")
    @Operation(summary = "获取正在输入的用户（私信）")
    public ResponseEntity<ApiResponse<List<TypingDTO.TypingResponse>>> getTypingUsersDirect(
            @PathVariable Long recipientId) {
        List<TypingStatus> typingUsers = typingService.getTypingUsersDirect(recipientId);
        
        List<TypingDTO.TypingResponse> response = typingUsers.stream()
                .map(typingService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/typing/stream/{streamId}")
    @Operation(summary = "获取正在输入的用户（频道）")
    public ResponseEntity<ApiResponse<List<TypingDTO.TypingResponse>>> getTypingUsersStream(
            @PathVariable Long streamId,
            @RequestParam(required = false) String topic) {
        List<TypingStatus> typingUsers = typingService.getTypingUsersStream(streamId, topic);
        
        List<TypingDTO.TypingResponse> response = typingUsers.stream()
                .map(typingService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/typing/direct/{recipientId}/count")
    @Operation(summary = "统计正在输入的用户数量")
    public ResponseEntity<ApiResponse<Long>> countTypingUsers(
            @PathVariable Long recipientId) {
        Long count = typingService.countTypingUsers(recipientId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/typing/direct/{recipientId}/summary")
    @Operation(summary = "获取输入状态摘要")
    public ResponseEntity<ApiResponse<TypingDTO.TypingSummary>> getTypingSummary(
            @PathVariable Long recipientId) {
        TypingDTO.TypingSummary summary = typingService.getTypingSummary(recipientId);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
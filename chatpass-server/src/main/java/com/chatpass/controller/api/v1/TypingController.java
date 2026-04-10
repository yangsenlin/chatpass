package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.TypingDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.TypingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Typing 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Typing", description = "输入状态 API")
public class TypingController {

    private final TypingService typingService;
    private final SecurityUtil securityUtil;

    @PostMapping("/typing/start")
    @Operation(summary = "开始输入")
    public ResponseEntity<ApiResponse<Void>> startTyping(@RequestBody TypingDTO.StartRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        typingService.startTyping(userId, realmId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/typing/stop")
    @Operation(summary = "停止输入")
    public ResponseEntity<ApiResponse<Void>> stopTyping(@RequestBody TypingDTO.StopRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        typingService.stopTyping(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/typing/status")
    @Operation(summary = "获取输入状态")
    public ResponseEntity<ApiResponse<TypingDTO.StatusResponse>> getTypingStatus(
            @RequestParam Long recipientId,
            @RequestParam(required = false) String topic) {
        
        TypingDTO.StatusResponse response = typingService.getTypingStatus(recipientId, topic);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
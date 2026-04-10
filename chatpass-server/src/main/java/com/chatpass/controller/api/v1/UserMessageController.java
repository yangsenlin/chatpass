package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.UserMessageDTO;
import com.chatpass.service.UserMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserMessage 控制器
 * 
 * 用户消息状态管理（已读、标记等）
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Messages", description = "用户消息状态 API")
public class UserMessageController {

    private final UserMessageService userMessageService;

    @PostMapping("/messages/flags")
    @Operation(summary = "更新消息 Flags", description = "标记/取消标记消息")
    public ResponseEntity<ApiResponse<UserMessageDTO.FlagsResponse>> updateFlags(
            @RequestBody UserMessageDTO.FlagsRequest request) {
        
        // TODO: 从 SecurityContext 获取用户信息
        Long userId = 1L;
        
        UserMessageDTO.FlagsResponse response = userMessageService.updateFlags(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/messages/mark_all_as_read")
    @Operation(summary = "标记全部已读")
    public ResponseEntity<ApiResponse<UserMessageDTO.FlagsResponse>> markAllAsRead() {
        Long userId = 1L;
        
        UserMessageDTO.FlagsResponse response = userMessageService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}/flags")
    @Operation(summary = "获取消息 Flags")
    public ResponseEntity<ApiResponse<List<String>>> getMessageFlags(
            @PathVariable Long messageId) {
        
        Long userId = 1L;
        
        List<String> flags = userMessageService.getMessageFlags(userId, messageId);
        return ResponseEntity.ok(ApiResponse.success(flags));
    }

    @GetMapping("/unread")
    @Operation(summary = "获取未读消息摘要")
    public ResponseEntity<ApiResponse<UserMessageDTO.UnreadSummary>> getUnreadSummary() {
        Long userId = 1L;
        
        UserMessageDTO.UnreadSummary summary = userMessageService.getUnreadSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
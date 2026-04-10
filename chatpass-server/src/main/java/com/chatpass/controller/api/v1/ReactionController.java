package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.ReactionDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.ReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Reaction 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "消息表情反应 API")
public class ReactionController {

    private final ReactionService reactionService;
    private final SecurityUtil securityUtil;

    @PostMapping("/messages/{messageId}/reactions")
    @Operation(summary = "添加表情反应")
    public ResponseEntity<ApiResponse<ReactionDTO.Response>> addReaction(
            @PathVariable Long messageId,
            @RequestBody ReactionDTO.AddRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        request.setMessageId(messageId);
        ReactionDTO.Response response = reactionService.addReaction(userId, realmId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/messages/{messageId}/reactions")
    @Operation(summary = "移除表情反应")
    public ResponseEntity<ApiResponse<Void>> removeReaction(
            @PathVariable Long messageId,
            @RequestParam String emojiCode) {
        
        Long userId = securityUtil.getCurrentUserId();
        reactionService.removeReaction(userId, messageId, emojiCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/messages/{messageId}/reactions")
    @Operation(summary = "获取消息的所有反应")
    public ResponseEntity<ApiResponse<ReactionDTO.ListResponse>> getMessageReactions(
            @PathVariable Long messageId) {
        
        ReactionDTO.ListResponse response = reactionService.getMessageReactions(messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/messages/reactions")
    @Operation(summary = "批量获取多条消息的反应")
    public ResponseEntity<ApiResponse<Map<Long, ReactionDTO.ListResponse>>> getMessagesReactions(
            @RequestBody List<Long> messageIds) {
        
        Map<Long, ReactionDTO.ListResponse> response = reactionService.getReactionsForMessages(messageIds);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}/reactions/mine")
    @Operation(summary = "获取我在此消息上的反应")
    public ResponseEntity<ApiResponse<List<ReactionDTO.Response>>> getMyReactions(
            @PathVariable Long messageId) {
        
        Long userId = securityUtil.getCurrentUserId();
        List<ReactionDTO.Response> response = reactionService.getUserReactionsOnMessage(userId, messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
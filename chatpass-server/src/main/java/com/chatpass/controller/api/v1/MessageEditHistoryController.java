package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.MessageEditDTO;
import com.chatpass.entity.Message;
import com.chatpass.entity.MessageEditHistory;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.MessageEditHistoryService;
import com.chatpass.repository.MessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MessageEditHistory 控制器
 * 
 * 消息编辑历史 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Message Edit History", description = "消息编辑历史 API")
public class MessageEditHistoryController {

    private final MessageEditHistoryService editHistoryService;
    private final MessageRepository messageRepository;
    private final SecurityUtil securityUtil;

    @GetMapping("/messages/{messageId}/history")
    @Operation(summary = "获取消息编辑历史")
    public ResponseEntity<ApiResponse<List<MessageEditDTO.EditHistoryResponse>>> getEditHistory(
            @PathVariable Long messageId) {
        List<MessageEditHistory> history = editHistoryService.getEditHistory(messageId);
        
        List<MessageEditDTO.EditHistoryResponse> response = history.stream()
                .map(editHistoryService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}/history/latest")
    @Operation(summary = "获取最近编辑")
    public ResponseEntity<ApiResponse<MessageEditDTO.EditHistoryResponse>> getLatestEdit(
            @PathVariable Long messageId) {
        MessageEditHistory latest = editHistoryService.getLatestEdit(messageId)
                .orElseThrow(() -> new IllegalArgumentException("没有编辑历史"));
        
        return ResponseEntity.ok(ApiResponse.success(editHistoryService.toResponse(latest)));
    }

    @GetMapping("/messages/{messageId}/history/summary")
    @Operation(summary = "获取编辑摘要")
    public ResponseEntity<ApiResponse<MessageEditDTO.EditSummary>> getEditSummary(
            @PathVariable Long messageId) {
        MessageEditDTO.EditSummary summary = editHistoryService.getEditSummary(messageId);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/messages/{messageId}/history/content")
    @Operation(summary = "获取内容编辑历史")
    public ResponseEntity<ApiResponse<List<MessageEditDTO.EditHistoryResponse>>> getContentEditHistory(
            @PathVariable Long messageId) {
        List<MessageEditHistory> history = editHistoryService.getContentEdits(messageId);
        
        List<MessageEditDTO.EditHistoryResponse> response = history.stream()
                .map(editHistoryService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}/history/topic")
    @Operation(summary = "获取 Topic 编辑历史")
    public ResponseEntity<ApiResponse<List<MessageEditDTO.EditHistoryResponse>>> getTopicEditHistory(
            @PathVariable Long messageId) {
        List<MessageEditHistory> history = editHistoryService.getTopicEdits(messageId);
        
        List<MessageEditDTO.EditHistoryResponse> response = history.stream()
                .map(editHistoryService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}/history/count")
    @Operation(summary = "获取编辑次数")
    public ResponseEntity<ApiResponse<Long>> getEditCount(@PathVariable Long messageId) {
        Long count = editHistoryService.getEditCount(messageId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/messages/{messageId}/restore/{historyId}")
    @Operation(summary = "恢复到历史版本")
    public ResponseEntity<ApiResponse<Void>> restoreToVersion(
            @PathVariable Long messageId,
            @PathVariable Long historyId) {
        editHistoryService.restoreToVersion(messageId, historyId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me/edit_history")
    @Operation(summary = "获取我的编辑历史")
    public ResponseEntity<ApiResponse<List<MessageEditDTO.EditHistoryResponse>>> getMyEditHistory() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<MessageEditHistory> history = editHistoryService.getUserEdits(userId);
        
        List<MessageEditDTO.EditHistoryResponse> response = history.stream()
                .limit(50) // 限制返回数量
                .map(editHistoryService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/messages/{messageId}")
    @Operation(summary = "编辑消息")
    public ResponseEntity<ApiResponse<Void>> editMessage(
            @PathVariable Long messageId,
            @RequestBody MessageEditDTO.EditRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        // 获取原消息
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));
        
        // 记录编辑历史
        editHistoryService.recordEdit(
                messageId,
                userId,
                message.getContent(),
                message.getSubject(),
                request.getContent(),
                request.getTopic()
        );
        
        // 更新消息内容（通过 MessageService）
        // MessageService 的 updateMessage 方法会处理实际更新
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.ReadReceiptDTO;
import com.chatpass.entity.ReadReceipt;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.ReadReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReadReceipt 控制器
 * 
 * 阅读回执 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Read Receipts", description = "阅读回执 API")
public class ReadReceiptController {

    private final ReadReceiptService readReceiptService;
    private final SecurityUtil securityUtil;

    @PostMapping("/messages/{id}/read")
    @Operation(summary = "标记消息已读")
    public ResponseEntity<ApiResponse<ReadReceiptDTO.Response>> markAsRead(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        
        ReadReceipt receipt = readReceiptService.markAsRead(id, userId);
        
        return ResponseEntity.ok(ApiResponse.success(toResponse(receipt)));
    }

    @PostMapping("/messages/read/batch")
    @Operation(summary = "批量标记已读")
    public ResponseEntity<ApiResponse<ReadReceiptDTO.BatchResponse>> markBatchAsRead(
            @RequestBody ReadReceiptDTO.BatchRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<ReadReceipt> receipts = readReceiptService.markBatchAsRead(request.getMessageIds(), userId);
        
        List<ReadReceiptDTO.Response> responses = receipts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(ReadReceiptDTO.BatchResponse.builder()
                .receipts(responses)
                .count(responses.size())
                .build()));
    }

    @GetMapping("/messages/{id}/read_status")
    @Operation(summary = "获取消息阅读状态")
    public ResponseEntity<ApiResponse<ReadReceiptDTO.StatusResponse>> getReadStatus(@PathVariable Long id) {
        List<ReadReceipt> receipts = readReceiptService.getMessageReadStatus(id);
        Long count = readReceiptService.getReadCount(id);
        
        List<ReadReceiptDTO.ReaderInfo> readers = receipts.stream()
                .map(r -> ReadReceiptDTO.ReaderInfo.builder()
                        .userId(r.getUser().getId())
                        .userName(r.getUser().getFullName())
                        .readAt(r.getReadAt().toString())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(ReadReceiptDTO.StatusResponse.builder()
                .messageId(id)
                .readCount(count)
                .readers(readers)
                .build()));
    }

    @GetMapping("/users/me/read_history")
    @Operation(summary = "获取已读历史")
    public ResponseEntity<ApiResponse<List<ReadReceiptDTO.Response>>> getReadHistory() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<ReadReceipt> receipts = readReceiptService.getUserReadHistory(userId);
        
        List<ReadReceiptDTO.Response> responses = receipts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/messages/{id}/read_by/{userId}")
    @Operation(summary = "检查是否已读")
    public ResponseEntity<ApiResponse<Boolean>> checkReadByUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        boolean read = readReceiptService.isReadByUser(id, userId);
        return ResponseEntity.ok(ApiResponse.success(read));
    }

    @GetMapping("/conversations/{recipientId}/last_read")
    @Operation(summary = "获取对话最后已读时间")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLastReadTime(@PathVariable Long recipientId) {
        Long userId = securityUtil.getCurrentUserId();
        
        java.time.LocalDateTime lastRead = readReceiptService.getLastReadTime(userId, recipientId);
        
        Map<String, String> result = Map.of(
                "recipient_id", recipientId.toString(),
                "last_read_at", lastRead != null ? lastRead.toString() : "never"
        );
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private ReadReceiptDTO.Response toResponse(ReadReceipt receipt) {
        return ReadReceiptDTO.Response.builder()
                .id(receipt.getId())
                .messageId(receipt.getMessage().getId())
                .userId(receipt.getUser().getId())
                .userName(receipt.getUser().getFullName())
                .readAt(receipt.getReadAt().toString())
                .build();
    }
}
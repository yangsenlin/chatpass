package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.AttachmentDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Attachment 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "文件附件 API")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final SecurityUtil securityUtil;

    @PostMapping("/attachments")
    @Operation(summary = "上传文件")
    public ResponseEntity<ApiResponse<AttachmentDTO.Response>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long messageId) throws IOException {
        
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        AttachmentDTO.Response response = attachmentService.upload(userId, realmId, messageId, file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/attachments/{attachmentId}/attach")
    @Operation(summary = "关联附件到消息")
    public ResponseEntity<ApiResponse<Void>> attachToMessage(
            @PathVariable Long attachmentId,
            @RequestParam Long messageId) {
        
        attachmentService.attachToMessage(attachmentId, messageId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/messages/{messageId}/attachments")
    @Operation(summary = "获取消息的附件")
    public ResponseEntity<ApiResponse<AttachmentDTO.ListResponse>> getMessageAttachments(
            @PathVariable Long messageId) {
        
        AttachmentDTO.ListResponse response = attachmentService.getMessageAttachments(messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "删除附件")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long attachmentId) {
        Long userId = securityUtil.getCurrentUserId();
        attachmentService.delete(attachmentId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me/storage")
    @Operation(summary = "获取用户存储使用量")
    public ResponseEntity<ApiResponse<Long>> getStorageUsage() {
        Long userId = securityUtil.getCurrentUserId();
        Long usage = attachmentService.getUserStorageUsage(userId);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }
}
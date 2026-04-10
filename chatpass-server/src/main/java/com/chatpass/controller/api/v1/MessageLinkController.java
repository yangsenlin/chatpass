package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.MessageDTO;
import com.chatpass.dto.MessageLinkDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.MessageLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * MessageLink 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Message Links", description = "消息引用/转发 API")
public class MessageLinkController {

    private final MessageLinkService messageLinkService;
    private final SecurityUtil securityUtil;

    @PostMapping("/messages/{messageId}/references")
    @Operation(summary = "创建消息引用")
    public ResponseEntity<ApiResponse<MessageLinkDTO.Response>> createReference(
            @PathVariable Long messageId,
            @RequestBody MessageLinkDTO.CreateRequest request) {
        
        request.setMessageId(messageId);
        Long userId = securityUtil.getCurrentUserId();
        MessageLinkDTO.Response response = messageLinkService.createLink(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/messages/{messageId}/forward")
    @Operation(summary = "转发消息")
    public ResponseEntity<ApiResponse<MessageDTO.Response>> forwardMessage(
            @PathVariable Long messageId,
            @RequestBody MessageLinkDTO.ForwardRequest request) {
        
        request.setSourceMessageId(messageId);
        MessageDTO.Response response = messageLinkService.forwardMessage(
                securityUtil.getCurrentUserId(), 
                securityUtil.getCurrentRealmId(), 
                request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}/links")
    @Operation(summary = "获取消息引用关系")
    public ResponseEntity<ApiResponse<MessageLinkDTO.ListResponse>> getMessageLinks(
            @PathVariable Long messageId) {
        
        MessageLinkDTO.ListResponse response = messageLinkService.getMessageLinks(messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/messages/links/{linkId}")
    @Operation(summary = "删除引用链接")
    public ResponseEntity<ApiResponse<Void>> deleteLink(@PathVariable Long linkId) {
        Long userId = securityUtil.getCurrentUserId();
        messageLinkService.deleteLink(linkId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
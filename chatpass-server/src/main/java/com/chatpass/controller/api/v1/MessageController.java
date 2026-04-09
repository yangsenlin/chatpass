package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.MessageDTO;
import com.chatpass.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 * 
 * Zulip 消息 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "消息 API")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/messages")
    @Operation(summary = "发送消息", description = "发送消息到 Stream 或私信")
    public ResponseEntity<ApiResponse<MessageDTO.Response>> sendMessage(
            @RequestBody MessageDTO.SendRequest request) {
        
        // TODO: 从 SecurityContext 获取用户信息
        Long senderId = 1L;
        Long realmId = 1L;
        
        MessageDTO.Response response;
        
        if ("stream".equals(request.getType())) {
            response = messageService.sendStreamMessage(
                    realmId, senderId, request.getTo(), request.getSubject(), request.getContent());
        } else {
            response = messageService.sendDirectMessage(
                    realmId, senderId, List.of(request.getTo()), request.getContent());
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}")
    @Operation(summary = "获取消息详情")
    public ResponseEntity<ApiResponse<MessageDTO.Response>> getMessage(
            @PathVariable Long messageId,
            @RequestParam(required = false) Long realmId) {
        
        MessageDTO.Response response = messageService.getById(realmId != null ? realmId : 1L, messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/messages/{messageId}")
    @Operation(summary = "编辑消息")
    public ResponseEntity<ApiResponse<MessageDTO.Response>> editMessage(
            @PathVariable Long messageId,
            @RequestBody MessageDTO.UpdateRequest request) {
        
        // TODO: 从 SecurityContext 获取用户信息
        Long userId = 1L;
        Long realmId = 1L;
        
        MessageDTO.Response response = messageService.update(
                realmId, messageId, userId, request.getSubject(), request.getContent());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/streams/{streamId}/messages")
    @Operation(summary = "获取 Stream 消息")
    public ResponseEntity<ApiResponse<MessageDTO.ListResponse>> getStreamMessages(
            @PathVariable Long streamId,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        
        // TODO: 从 SecurityContext 获取 realmId
        Long realmId = 1L;
        
        MessageDTO.ListResponse response = messageService.getStreamMessages(
                realmId, streamId, topic, page, pageSize);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
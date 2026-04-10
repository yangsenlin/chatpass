package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.WebhookDTO;
import com.chatpass.entity.Webhook;
import com.chatpass.entity.WebhookLog;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Webhook 控制器
 * 
 * Webhook 管理 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook API")
public class WebhookController {

    private final WebhookService webhookService;
    private final SecurityUtil securityUtil;

    @PostMapping("/webhooks")
    @Operation(summary = "创建 Webhook")
    public ResponseEntity<ApiResponse<WebhookDTO.WebhookResponse>> createWebhook(
            @RequestBody WebhookDTO.CreateWebhookRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        Webhook webhook = webhookService.createWebhook(userId, realmId, 
                request.getName(), request.getWebhookUrl(), 
                request.getTargetStreamId(), request.getDefaultTopic(), 
                request.getEventTypes());
        
        return ResponseEntity.ok(ApiResponse.success(webhookService.toResponse(webhook)));
    }

    @GetMapping("/webhooks/{webhookId}")
    @Operation(summary = "获取 Webhook 详情")
    public ResponseEntity<ApiResponse<WebhookDTO.WebhookResponse>> getWebhook(
            @PathVariable Long webhookId) {
        Webhook webhook = webhookService.getWebhook(webhookId);
        
        return ResponseEntity.ok(ApiResponse.success(webhookService.toResponse(webhook)));
    }

    @GetMapping("/webhooks")
    @Operation(summary = "获取我的 Webhook 列表")
    public ResponseEntity<ApiResponse<List<WebhookDTO.WebhookResponse>>> getMyWebhooks() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<Webhook> webhooks = webhookService.getOwnerWebhooks(userId);
        
        List<WebhookDTO.WebhookResponse> response = webhooks.stream()
                .map(webhookService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/realm/webhooks")
    @Operation(summary = "获取 Realm 的 Webhook 列表")
    public ResponseEntity<ApiResponse<List<WebhookDTO.WebhookResponse>>> getRealmWebhooks() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<Webhook> webhooks = webhookService.getRealmWebhooks(realmId);
        
        List<WebhookDTO.WebhookResponse> response = webhooks.stream()
                .map(webhookService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/webhooks/{webhookId}")
    @Operation(summary = "更新 Webhook")
    public ResponseEntity<ApiResponse<WebhookDTO.WebhookResponse>> updateWebhook(
            @PathVariable Long webhookId,
            @RequestBody WebhookDTO.UpdateWebhookRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        Webhook webhook = webhookService.updateWebhook(webhookId, userId, 
                request.getName(), request.getWebhookUrl(), 
                request.getEventTypes(), request.getIsActive());
        
        return ResponseEntity.ok(ApiResponse.success(webhookService.toResponse(webhook)));
    }

    @DeleteMapping("/webhooks/{webhookId}")
    @Operation(summary = "删除 Webhook")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(@PathVariable Long webhookId) {
        Long userId = securityUtil.getCurrentUserId();
        
        webhookService.deleteWebhook(webhookId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/webhooks/{webhookId}/reset-key")
    @Operation(summary = "重置 Webhook Key")
    public ResponseEntity<ApiResponse<WebhookDTO.WebhookKeyResponse>> resetWebhookKey(
            @PathVariable Long webhookId) {
        Long userId = securityUtil.getCurrentUserId();
        
        String newKey = webhookService.resetWebhookKey(webhookId, userId);
        
        WebhookDTO.WebhookKeyResponse response = WebhookDTO.WebhookKeyResponse.builder()
                .webhookId(webhookId)
                .webhookKey(newKey)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/webhooks/{webhookId}/logs")
    @Operation(summary = "获取 Webhook 调用日志")
    public ResponseEntity<ApiResponse<List<WebhookDTO.WebhookLogResponse>>> getWebhookLogs(
            @PathVariable Long webhookId) {
        List<WebhookLog> logs = webhookService.getWebhookLogs(webhookId);
        
        List<WebhookDTO.WebhookLogResponse> response = logs.stream()
                .map(webhookService::toLogResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/webhooks/{webhookId}/logs/failed")
    @Operation(summary = "获取失败的调用日志")
    public ResponseEntity<ApiResponse<List<WebhookDTO.WebhookLogResponse>>> getFailedLogs(
            @PathVariable Long webhookId) {
        List<WebhookLog> logs = webhookService.getFailedLogs(webhookId);
        
        List<WebhookDTO.WebhookLogResponse> response = logs.stream()
                .map(webhookService::toLogResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/webhooks/{webhookId}/summary")
    @Operation(summary = "获取 Webhook 统计摘要")
    public ResponseEntity<ApiResponse<WebhookDTO.WebhookSummary>> getWebhookSummary(
            @PathVariable Long webhookId) {
        WebhookDTO.WebhookSummary summary = webhookService.getSummary(webhookId);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/webhooks/count")
    @Operation(summary = "统计 Webhook 数量")
    public ResponseEntity<ApiResponse<Long>> countWebhooks() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        Long count = webhookService.countWebhooks(realmId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // Webhook 接收端点（供外部调用）

    @PostMapping("/webhooks/invoke/{webhookKey}")
    @Operation(summary = "触发 Webhook（外部调用）")
    public ResponseEntity<ApiResponse<WebhookDTO.InvokeResponse>> invokeWebhook(
            @PathVariable String webhookKey,
            @RequestBody WebhookDTO.InvokeRequest request) {
        
        // 异步触发
        webhookService.invokeWebhook(webhookKey, request.getEventType(), request.getData());
        
        WebhookDTO.InvokeResponse response = WebhookDTO.InvokeResponse.builder()
                .webhookKey(webhookKey)
                .eventType(request.getEventType())
                .invoked(true)
                .message("Webhook triggered successfully")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/webhooks/test/{webhookId}")
    @Operation(summary = "测试 Webhook")
    public ResponseEntity<ApiResponse<WebhookDTO.TestResponse>> testWebhook(
            @PathVariable Long webhookId) {
        Long userId = securityUtil.getCurrentUserId();
        
        Webhook webhook = webhookService.getWebhook(webhookId);
        
        // 验证所有者
        if (!webhook.getOwnerId().equals(userId)) {
            throw new IllegalStateException("不是 Webhook 所有者");
        }
        
        // 测试调用
        try {
            WebhookLog log = webhookService.invokeWebhook(
                    webhook.getWebhookKey(), 
                    "TEST", 
                    "{\"test\": true}").get();
            
            WebhookDTO.TestResponse response = WebhookDTO.TestResponse.builder()
                    .webhookId(webhookId)
                    .success(log.getResult().equals(WebhookLog.RESULT_SUCCESS))
                    .responseStatus(log.getResponseStatus())
                    .responseTimeMs(log.getResponseTimeMs())
                    .errorMessage(log.getErrorMessage())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            WebhookDTO.TestResponse response = WebhookDTO.TestResponse.builder()
                    .webhookId(webhookId)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        }
    }
}
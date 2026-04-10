package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.PushDTO;
import com.chatpass.entity.MobilePush;
import com.chatpass.entity.PushNotification;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Push Notification 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Push Notifications", description = "推送通知 API")
public class PushNotificationController {

    private final PushNotificationService pushService;
    private final SecurityUtil securityUtil;

    @PostMapping("/push/configs")
    @Operation(summary = "创建推送配置")
    public ResponseEntity<ApiResponse<PushDTO.PushConfigResponse>> createPushConfig(
            @RequestBody PushDTO.CreatePushConfigRequest request) {
        Long realmId = securityUtil.getCurrentRealmId();

        MobilePush push = pushService.createPushConfig(
                realmId, request.getPushType(), request.getProjectId(),
                request.getApiKey(), request.getSenderId());

        return ResponseEntity.ok(ApiResponse.success(toPushConfigResponse(push)));
    }

    @GetMapping("/push/configs")
    @Operation(summary = "获取推送配置列表")
    public ResponseEntity<ApiResponse<List<PushDTO.PushConfigResponse>>> getPushConfigs() {
        Long realmId = securityUtil.getCurrentRealmId();

        List<MobilePush> configs = pushService.getRealmPushConfigs(realmId);

        List<PushDTO.PushConfigResponse> response = configs.stream()
                .map(this::toPushConfigResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/push/configs/{configId}")
    @Operation(summary = "获取推送配置详情")
    public ResponseEntity<ApiResponse<PushDTO.PushConfigResponse>> getPushConfig(
            @PathVariable Long configId) {
        MobilePush push = pushService.getPushConfig(configId)
                .orElseThrow(() -> new IllegalArgumentException("推送配置不存在"));

        return ResponseEntity.ok(ApiResponse.success(toPushConfigResponse(push)));
    }

    @PostMapping("/push/configs/{configId}/activate")
    @Operation(summary = "激活推送配置")
    public ResponseEntity<ApiResponse<PushDTO.PushConfigResponse>> activatePushConfig(
            @PathVariable Long configId) {
        MobilePush push = pushService.updateConfigStatus(configId, true);

        return ResponseEntity.ok(ApiResponse.success(toPushConfigResponse(push)));
    }

    @PostMapping("/push/configs/{configId}/deactivate")
    @Operation(summary = "停用推送配置")
    public ResponseEntity<ApiResponse<PushDTO.PushConfigResponse>> deactivatePushConfig(
            @PathVariable Long configId) {
        MobilePush push = pushService.updateConfigStatus(configId, false);

        return ResponseEntity.ok(ApiResponse.success(toPushConfigResponse(push)));
    }

    @DeleteMapping("/push/configs/{configId}")
    @Operation(summary = "删除推送配置")
    public ResponseEntity<ApiResponse<Void>> deletePushConfig(@PathVariable Long configId) {
        pushService.deletePushConfig(configId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/push/send")
    @Operation(summary = "发送推送通知")
    public ResponseEntity<ApiResponse<PushDTO.NotificationResponse>> sendPush(
            @RequestBody PushDTO.SendPushRequest request) {
        PushNotification notification = pushService.createNotification(
                request.getPushConfigId(), request.getUserId(), request.getMessageId(),
                request.getDeviceToken(), request.getNotificationType(),
                request.getTitle(), request.getBody(), request.getDataPayload());

        pushService.sendPush(notification.getId());

        return ResponseEntity.ok(ApiResponse.success(toNotificationResponse(notification)));
    }

    @PostMapping("/push/configs/{configId}/batch-send")
    @Operation(summary = "批量发送推送")
    public ResponseEntity<ApiResponse<Integer>> batchSendPush(@PathVariable Long configId) {
        int sent = pushService.sendBatchNotifications(configId);

        return ResponseEntity.ok(ApiResponse.success(sent));
    }

    @GetMapping("/push/notifications")
    @Operation(summary = "获取我的推送通知")
    public ResponseEntity<ApiResponse<List<PushDTO.NotificationResponse>>> getMyNotifications() {
        Long userId = securityUtil.getCurrentUserId();

        List<PushNotification> notifications = pushService.getUserNotifications(userId);

        List<PushDTO.NotificationResponse> response = notifications.stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/push/stats")
    @Operation(summary = "获取推送统计")
    public ResponseEntity<ApiResponse<PushDTO.PushStats>> getPushStats() {
        PushDTO.PushStats stats = PushDTO.PushStats.builder()
                .pendingCount(pushService.countNotificationsByStatus("PENDING"))
                .sentCount(pushService.countNotificationsByStatus("SENT"))
                .failedCount(pushService.countNotificationsByStatus("FAILED"))
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    private PushDTO.PushConfigResponse toPushConfigResponse(MobilePush push) {
        return PushDTO.PushConfigResponse.builder()
                .id(push.getId())
                .realmId(push.getRealmId())
                .pushType(push.getPushType())
                .projectId(push.getProjectId())
                .senderId(push.getSenderId())
                .isActive(push.getIsActive())
                .batchSize(push.getBatchSize())
                .dateCreated(push.getDateCreated() != null ? push.getDateCreated().toString() : null)
                .build();
    }

    private PushDTO.NotificationResponse toNotificationResponse(PushNotification notification) {
        return PushDTO.NotificationResponse.builder()
                .id(notification.getId())
                .pushConfigId(notification.getPushConfigId())
                .userId(notification.getUserId())
                .messageId(notification.getMessageId())
                .deviceToken(notification.getDeviceToken())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .dateCreated(notification.getDateCreated() != null ? notification.getDateCreated().toString() : null)
                .dateSent(notification.getDateSent() != null ? notification.getDateSent().toString() : null)
                .build();
    }
}
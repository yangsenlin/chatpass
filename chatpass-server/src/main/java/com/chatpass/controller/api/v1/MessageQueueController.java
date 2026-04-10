package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.QueueDTO;
import com.chatpass.entity.MessageQueue;
import com.chatpass.entity.QueuedMessage;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.MessageQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Message Queue 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Message Queue", description = "消息队列 API")
public class MessageQueueController {

    private final MessageQueueService queueService;
    private final SecurityUtil securityUtil;

    @PostMapping("/queues")
    @Operation(summary = "创建队列配置")
    public ResponseEntity<ApiResponse<QueueDTO.QueueResponse>> createQueue(
            @RequestBody QueueDTO.CreateQueueRequest request) {
        Long realmId = securityUtil.getCurrentRealmId();

        MessageQueue queue = queueService.createQueue(
                realmId, request.getQueueName(), request.getQueueType(),
                request.getBrokerUrl(), request.getExchangeName(), request.getRoutingKey());

        return ResponseEntity.ok(ApiResponse.success(toQueueResponse(queue)));
    }

    @GetMapping("/queues")
    @Operation(summary = "获取队列列表")
    public ResponseEntity<ApiResponse<List<QueueDTO.QueueResponse>>> getQueues() {
        Long realmId = securityUtil.getCurrentRealmId();

        List<MessageQueue> queues = queueService.getRealmQueues(realmId);

        List<QueueDTO.QueueResponse> response = queues.stream()
                .map(this::toQueueResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/queues/{queueId}")
    @Operation(summary = "获取队列详情")
    public ResponseEntity<ApiResponse<QueueDTO.QueueResponse>> getQueue(
            @PathVariable Long queueId) {
        MessageQueue queue = queueService.getQueue(queueId)
                .orElseThrow(() -> new IllegalArgumentException("队列不存在"));

        return ResponseEntity.ok(ApiResponse.success(toQueueResponse(queue)));
    }

    @PostMapping("/queues/{queueId}/activate")
    @Operation(summary = "激活队列")
    public ResponseEntity<ApiResponse<QueueDTO.QueueResponse>> activateQueue(
            @PathVariable Long queueId) {
        MessageQueue queue = queueService.updateQueueStatus(queueId, true);

        return ResponseEntity.ok(ApiResponse.success(toQueueResponse(queue)));
    }

    @PostMapping("/queues/{queueId}/deactivate")
    @Operation(summary = "停用队列")
    public ResponseEntity<ApiResponse<QueueDTO.QueueResponse>> deactivateQueue(
            @PathVariable Long queueId) {
        MessageQueue queue = queueService.updateQueueStatus(queueId, false);

        return ResponseEntity.ok(ApiResponse.success(toQueueResponse(queue)));
    }

    @DeleteMapping("/queues/{queueId}")
    @Operation(summary = "删除队列")
    public ResponseEntity<ApiResponse<Void>> deleteQueue(@PathVariable Long queueId) {
        queueService.deleteQueue(queueId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/queues/enqueue")
    @Operation(summary = "添加消息到队列")
    public ResponseEntity<ApiResponse<QueueDTO.QueuedMessageResponse>> enqueue(
            @RequestBody QueueDTO.EnqueueRequest request) {
        QueuedMessage qm = queueService.enqueue(
                request.getQueueId(), request.getMessageId(), request.getPayload());

        return ResponseEntity.ok(ApiResponse.success(toQueuedMessageResponse(qm)));
    }

    @PostMapping("/queues/messages/{messageId}/send")
    @Operation(summary = "发送队列消息")
    public ResponseEntity<ApiResponse<Boolean>> sendMessage(@PathVariable Long messageId) {
        boolean result = queueService.sendMessage(messageId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/queues/pending")
    @Operation(summary = "获取待处理消息")
    public ResponseEntity<ApiResponse<List<QueueDTO.QueuedMessageResponse>>> getPendingMessages() {
        List<QueuedMessage> messages = queueService.getPendingMessages(3);

        List<QueueDTO.QueuedMessageResponse> response = messages.stream()
                .map(this::toQueuedMessageResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/queues/{queueId}/stats")
    @Operation(summary = "获取队列统计")
    public ResponseEntity<ApiResponse<QueueDTO.QueueStats>> getQueueStats(
            @PathVariable Long queueId) {
        MessageQueue queue = queueService.getQueue(queueId)
                .orElseThrow(() -> new IllegalArgumentException("队列不存在"));

        QueueDTO.QueueStats stats = QueueDTO.QueueStats.builder()
                .queueId(queueId)
                .queueName(queue.getQueueName())
                .pendingCount(queueService.countQueueMessages(queueId, "PENDING"))
                .sentCount(queueService.countQueueMessages(queueId, "SENT"))
                .failedCount(queueService.countQueueMessages(queueId, "FAILED"))
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    private QueueDTO.QueueResponse toQueueResponse(MessageQueue queue) {
        return QueueDTO.QueueResponse.builder()
                .id(queue.getId())
                .realmId(queue.getRealmId())
                .queueName(queue.getQueueName())
                .queueType(queue.getQueueType())
                .brokerUrl(queue.getBrokerUrl())
                .exchangeName(queue.getExchangeName())
                .routingKey(queue.getRoutingKey())
                .isActive(queue.getIsActive())
                .maxRetry(queue.getMaxRetry())
                .retryDelaySeconds(queue.getRetryDelaySeconds())
                .dateCreated(queue.getDateCreated() != null ? queue.getDateCreated().toString() : null)
                .build();
    }

    private QueueDTO.QueuedMessageResponse toQueuedMessageResponse(QueuedMessage qm) {
        return QueueDTO.QueuedMessageResponse.builder()
                .id(qm.getId())
                .queueId(qm.getQueueId())
                .messageId(qm.getMessageId())
                .payload(qm.getPayload())
                .status(qm.getStatus())
                .retryCount(qm.getRetryCount())
                .errorMessage(qm.getErrorMessage())
                .dateQueued(qm.getDateQueued() != null ? qm.getDateQueued().toString() : null)
                .dateSent(qm.getDateSent() != null ? qm.getDateSent().toString() : null)
                .build();
    }
}
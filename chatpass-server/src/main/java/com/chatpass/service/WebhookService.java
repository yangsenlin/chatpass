package com.chatpass.service;

import com.chatpass.dto.WebhookDTO;
import com.chatpass.entity.Webhook;
import com.chatpass.entity.WebhookLog;
import com.chatpass.repository.WebhookRepository;
import com.chatpass.repository.WebhookLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * WebhookService
 * 
 * Webhook 管理和调用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookLogRepository logRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AuditLogService auditLogService;

    /**
     * 创建 Webhook
     */
    @Transactional
    public Webhook createWebhook(Long ownerId, Long realmId, String name, String webhookUrl,
                                  Long targetStreamId, String defaultTopic, String eventTypes) {
        Webhook webhook = Webhook.builder()
                .name(name)
                .webhookKey(Webhook.generateWebhookKey())
                .webhookUrl(webhookUrl)
                .ownerId(ownerId)
                .realmId(realmId)
                .targetStreamId(targetStreamId)
                .defaultTopic(defaultTopic)
                .eventTypes(eventTypes)
                .isActive(true)
                .build();

        webhook = webhookRepository.save(webhook);

        auditLogService.logCreate(ownerId, AuditLogService.RESOURCE_BOT, webhook.getId(), webhook);

        log.info("Webhook created: {} for user {}", name, ownerId);

        return webhook;
    }

    /**
     * 更新 Webhook
     */
    @Transactional
    public Webhook updateWebhook(Long webhookId, Long ownerId, String name, String webhookUrl,
                                  String eventTypes, Boolean isActive) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook 不存在"));

        if (!webhook.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("不是 Webhook 所有者");
        }

        Webhook oldWebhook = webhook;

        if (name != null) webhook.setName(name);
        if (webhookUrl != null) webhook.setWebhookUrl(webhookUrl);
        if (eventTypes != null) webhook.setEventTypes(eventTypes);
        if (isActive != null) webhook.setIsActive(isActive);

        webhook = webhookRepository.save(webhook);

        auditLogService.logUpdate(ownerId, "WEBHOOK", webhook.getId(), oldWebhook, webhook);

        log.info("Webhook updated: {}", webhookId);

        return webhook;
    }

    /**
     * 删除 Webhook
     */
    @Transactional
    public void deleteWebhook(Long webhookId, Long ownerId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook 不存在"));

        if (!webhook.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("不是 Webhook 所有者");
        }

        webhook.setIsActive(false);
        webhookRepository.save(webhook);

        auditLogService.logDelete(ownerId, "WEBHOOK", webhook.getId(), webhook);

        log.info("Webhook deleted: {}", webhookId);
    }

    /**
     * 获取 Webhook 详情
     */
    public Webhook getWebhook(Long webhookId) {
        return webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook 不存在"));
    }

    /**
     * 通过 Key 获取 Webhook
     */
    public Optional<Webhook> getWebhookByKey(String webhookKey) {
        return webhookRepository.findByWebhookKey(webhookKey);
    }

    /**
     * 获取所有者的 Webhook
     */
    public List<Webhook> getOwnerWebhooks(Long ownerId) {
        return webhookRepository.findByOwnerId(ownerId);
    }

    /**
     * 获取 Realm 的 Webhook
     */
    public List<Webhook> getRealmWebhooks(Long realmId) {
        return webhookRepository.findByRealmId(realmId);
    }

    /**
     * 触发 Webhook（异步）
     */
    @Async
    @Transactional
    public CompletableFuture<WebhookLog> invokeWebhook(String webhookKey, String eventType, Object eventData) {
        Webhook webhook = webhookRepository.findByWebhookKey(webhookKey)
                .orElseThrow(() -> new IllegalArgumentException("无效的 Webhook Key"));

        if (!webhook.getIsActive()) {
            throw new IllegalStateException("Webhook 已禁用");
        }

        WebhookLog logEntry = WebhookLog.builder()
                .webhook(webhook)
                .eventType(eventType)
                .invokeTime(LocalDateTime.now())
                .build();

        try {
            // 序列化事件数据
            String eventJson = objectMapper.writeValueAsString(eventData);
            logEntry.setEventData(eventJson);

            // 构建请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 添加自定义请求头
            if (webhook.getRequestHeaders() != null && !webhook.getRequestHeaders().isEmpty()) {
                try {
                    // 解析 JSON 格式的自定义请求头
                    Map<String, String> customHeaders = objectMapper.readValue(
                            webhook.getRequestHeaders(), 
                            Map.class
                    );
                    customHeaders.forEach(headers::add);
                } catch (Exception e) {
                    log.warn("Failed to parse custom headers: {}", e.getMessage());
                }
            }

            // 构建请求体
            String requestBody = buildRequestBody(webhook, eventType, eventData);
            logEntry.setRequestBody(requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            logEntry.setRequestUrl(webhook.getWebhookUrl());
            logEntry.setRequestMethod(webhook.getRequestMethod());
            logEntry.setRequestHeaders(objectMapper.writeValueAsString(headers));

            // 发送请求
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    webhook.getWebhookUrl(),
                    HttpMethod.valueOf(webhook.getRequestMethod()),
                    entity,
                    String.class);
            long endTime = System.currentTimeMillis();

            logEntry.setResponseStatus(response.getStatusCode().value());
            logEntry.setResponseBody(response.getBody());
            logEntry.setResponseTimeMs(endTime - startTime);
            logEntry.setResult(WebhookLog.RESULT_SUCCESS);

            // 更新 Webhook 统计
            webhook.setLastInvoked(LocalDateTime.now());
            webhook.setLastResult("SUCCESS");
            webhook.setInvokeCount(webhook.getInvokeCount() + 1);
            webhook.setSuccessCount(webhook.getSuccessCount() + 1);
            webhookRepository.save(webhook);

            log.info("Webhook {} invoked successfully: {}", webhook.getName(), response.getStatusCode());

        } catch (Exception e) {
            logEntry.setResult(WebhookLog.RESULT_FAILURE);
            logEntry.setErrorMessage(e.getMessage());

            // 更新 Webhook 统计
            webhook.setLastInvoked(LocalDateTime.now());
            webhook.setLastResult("FAILURE");
            webhook.setInvokeCount(webhook.getInvokeCount() + 1);
            webhook.setFailureCount(webhook.getFailureCount() + 1);
            webhookRepository.save(webhook);

            log.error("Webhook {} invocation failed: {}", webhook.getName(), e.getMessage());

            // 重试逻辑
            if (logEntry.getRetryAttempt() < webhook.getRetryCount()) {
                log.info("Retrying webhook {} (attempt {})", webhook.getName(), logEntry.getRetryAttempt() + 1);
                
                // 保存需要的数据用于重试
                final Webhook retryWebhook = webhook;
                final String retryEventType = eventType;
                final String retryRequestBody = logEntry.getRequestBody();
                final String retryEventData = logEntry.getEventData();
                final int retryAttempt = logEntry.getRetryAttempt() + 1;
                final int retryDelayMs = retryWebhook.getRetryInterval() != null ? retryWebhook.getRetryInterval() * 1000 : 5000;
                
                // 异步重试
                CompletableFuture.runAsync(() -> {
                    try {
                        // 等待重试延迟
                        Thread.sleep(retryDelayMs);
                        
                        // 创建重试日志
                        WebhookLog retryLog = WebhookLog.builder()
                                .webhook(retryWebhook)
                                .eventType(retryEventType)
                                .invokeTime(LocalDateTime.now())
                                .retryAttempt(retryAttempt)
                                .build();
                        
                        retryLog.setEventData(retryEventData);
                        retryLog.setRequestUrl(retryWebhook.getWebhookUrl());
                        retryLog.setRequestMethod(retryWebhook.getRequestMethod());
                        
                        // 发送请求
                        HttpHeaders retryHeaders = new HttpHeaders();
                        retryHeaders.setContentType(MediaType.APPLICATION_JSON);
                        if (retryWebhook.getRequestHeaders() != null) {
                            try {
                                Map<String, String> customHeaders = objectMapper.readValue(
                                        retryWebhook.getRequestHeaders(),
                                        Map.class
                                );
                                customHeaders.forEach(retryHeaders::add);
                            } catch (Exception ex) {
                                log.warn("Failed to parse custom headers for retry: {}", ex.getMessage());
                            }
                        }
                        
                        HttpEntity<String> retryEntity = new HttpEntity<>(retryRequestBody, retryHeaders);
                        
                        long retryStartTime = System.currentTimeMillis();
                        ResponseEntity<String> retryResponse = restTemplate.exchange(
                                retryWebhook.getWebhookUrl(),
                                HttpMethod.valueOf(retryWebhook.getRequestMethod()),
                                retryEntity,
                                String.class);
                        long retryEndTime = System.currentTimeMillis();
                        
                        retryLog.setResponseStatus(retryResponse.getStatusCode().value());
                        retryLog.setResponseBody(retryResponse.getBody());
                        retryLog.setResponseTimeMs(retryEndTime - retryStartTime);
                        retryLog.setResult(WebhookLog.RESULT_SUCCESS);
                        
                        // 更新成功统计
                        retryWebhook.setLastResult("SUCCESS");
                        retryWebhook.setSuccessCount(retryWebhook.getSuccessCount() + 1);
                        webhookRepository.save(retryWebhook);
                        
                        logRepository.save(retryLog);
                        
                        log.info("Webhook {} retry succeeded", retryWebhook.getName());
                    } catch (Exception retryEx) {
                        log.error("Webhook {} retry failed: {}", retryWebhook.getName(), retryEx.getMessage());
                    }
                });
            }
        }

        logEntry = logRepository.save(logEntry);

        return CompletableFuture.completedFuture(logEntry);
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(Webhook webhook, String eventType, Object eventData) {
        try {
            if (webhook.getRequestBodyTemplate() != null) {
                // 使用模板
                return webhook.getRequestBodyTemplate();
            }
            // 默认格式
            WebhookDTO.WebhookPayload payload = WebhookDTO.WebhookPayload.builder()
                    .webhookKey(webhook.getWebhookKey())
                    .eventType(eventType)
                    .data(eventData)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Failed to build request body: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 获取 Webhook 日志
     */
    public List<WebhookLog> getWebhookLogs(Long webhookId) {
        return logRepository.findByWebhookId(webhookId);
    }

    /**
     * 获取失败的日志
     */
    public List<WebhookLog> getFailedLogs(Long webhookId) {
        return logRepository.findFailedLogs(webhookId);
    }

    /**
     * 重置 Webhook Key
     */
    @Transactional
    public String resetWebhookKey(Long webhookId, Long ownerId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook 不存在"));

        if (!webhook.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("不是 Webhook 所有者");
        }

        String newKey = Webhook.generateWebhookKey();
        webhook.setWebhookKey(newKey);
        webhookRepository.save(webhook);

        log.info("Webhook key reset: {}", webhookId);

        return newKey;
    }

    /**
     * 统计 Webhook 数量
     */
    public Long countWebhooks(Long realmId) {
        return webhookRepository.countByRealmId(realmId);
    }

    /**
     * 转换为 DTO
     */
    public WebhookDTO.WebhookResponse toResponse(Webhook webhook) {
        Long successCount = logRepository.countSuccessByWebhookId(webhook.getId());
        Long failureCount = logRepository.countFailureByWebhookId(webhook.getId());
        Double avgResponseTime = logRepository.avgResponseTime(webhook.getId());

        return WebhookDTO.WebhookResponse.builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .webhookKey(webhook.getWebhookKey())
                .webhookUrl(webhook.getWebhookUrl())
                .ownerId(webhook.getOwnerId())
                .realmId(webhook.getRealmId())
                .botId(webhook.getBotId())
                .targetStreamId(webhook.getTargetStreamId())
                .defaultTopic(webhook.getDefaultTopic())
                .eventTypes(webhook.getEventTypes())
                .description(webhook.getDescription())
                .isActive(webhook.getIsActive())
                .invokeCount(webhook.getInvokeCount())
                .successCount(successCount)
                .failureCount(failureCount)
                .successRate(webhook.getSuccessRate())
                .avgResponseTime(avgResponseTime)
                .lastInvoked(webhook.getLastInvoked() != null ? webhook.getLastInvoked().toString() : null)
                .lastResult(webhook.getLastResult())
                .dateCreated(webhook.getDateCreated().toString())
                .build();
    }

    /**
     * 转换日志为 DTO
     */
    public WebhookDTO.WebhookLogResponse toLogResponse(WebhookLog log) {
        return WebhookDTO.WebhookLogResponse.builder()
                .id(log.getId())
                .webhookId(log.getWebhook().getId())
                .webhookName(log.getWebhook().getName())
                .eventType(log.getEventType())
                .eventData(log.getEventData())
                .requestUrl(log.getRequestUrl())
                .requestMethod(log.getRequestMethod())
                .responseStatus(log.getResponseStatus())
                .responseBody(log.getResponseBody())
                .result(log.getResult())
                .errorMessage(log.getErrorMessage())
                .retryAttempt(log.getRetryAttempt())
                .invokeTime(log.getInvokeTime().toString())
                .responseTimeMs(log.getResponseTimeMs())
                .build();
    }

    /**
     * 获取 Webhook 统计摘要
     */
    public WebhookDTO.WebhookSummary getSummary(Long webhookId) {
        Webhook webhook = getWebhook(webhookId);
        
        Long total = logRepository.countByWebhookId(webhookId);
        Long success = logRepository.countSuccessByWebhookId(webhookId);
        Long failure = logRepository.countFailureByWebhookId(webhookId);
        Double avgTime = logRepository.avgResponseTime(webhookId);

        return WebhookDTO.WebhookSummary.builder()
                .webhookId(webhookId)
                .webhookName(webhook.getName())
                .totalInvocations(total)
                .successCount(success)
                .failureCount(failure)
                .successRate(total > 0 ? (double) success / total * 100 : 0)
                .avgResponseTime(avgTime)
                .lastInvoked(webhook.getLastInvoked() != null ? webhook.getLastInvoked().toString() : null)
                .build();
    }
}
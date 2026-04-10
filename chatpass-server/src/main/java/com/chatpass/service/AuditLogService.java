package com.chatpass.service;

import com.chatpass.dto.AuditLogDTO;
import com.chatpass.entity.AuditLog;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.AuditLogRepository;
import com.chatpass.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AuditLogService
 * 
 * 审计日志管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    // 资源类型常量（用于审计日志）
    public static final String RESOURCE_MESSAGE = "MESSAGE";
    public static final String RESOURCE_STREAM = "STREAM";
    public static final String RESOURCE_USER = "USER";
    public static final String RESOURCE_USER_GROUP = "USER_GROUP";
    public static final String RESOURCE_INVITE = "INVITE";
    public static final String RESOURCE_REACTION = "REACTION";
    public static final String RESOURCE_UPLOAD = "UPLOAD";
    public static final String RESOURCE_EMOJI = "EMOJI";
    public static final String RESOURCE_STAR = "STAR";
    public static final String RESOURCE_BOT = "BOT";
    public static final String RESOURCE_WEBHOOK = "WEBHOOK";

    private final AuditLogRepository auditLogRepository;
    private final UserProfileRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 记录操作日志
     */
    @Transactional
    public AuditLog log(Long userId, String eventType, String description, 
                        String resourceType, Long resourceId, 
                        Object oldValue, Object newValue,
                        String ipAddress, String userAgent) {
        UserProfile user = userRepository.findById(userId).orElse(null);
        
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userName(user != null ? user.getFullName() : "Unknown")
                .eventType(eventType)
                .eventDescription(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .realmId(user != null ? user.getRealm().getId() : null)
                .requestId(UUID.randomUUID().toString())
                .result(AuditLog.RESULT_SUCCESS)
                .build();

        // 序列化 old/new value
        if (oldValue != null) {
            try {
                auditLog.setOldValue(objectMapper.writeValueAsString(oldValue));
            } catch (Exception e) {
                log.warn("Failed to serialize old value: {}", e.getMessage());
            }
        }
        if (newValue != null) {
            try {
                auditLog.setNewValue(objectMapper.writeValueAsString(newValue));
            } catch (Exception e) {
                log.warn("Failed to serialize new value: {}", e.getMessage());
            }
        }

        auditLog = auditLogRepository.save(auditLog);

        log.debug("Audit log created: {} by user {}", eventType, userId);

        return auditLog;
    }

    /**
     * 记录成功操作
     */
    @Transactional
    public AuditLog logSuccess(Long userId, String eventType, String description,
                               String resourceType, Long resourceId) {
        return log(userId, eventType, description, resourceType, resourceId, null, null, null, null);
    }

    /**
     * 记录失败操作
     */
    @Transactional
    public AuditLog logFailure(Long userId, String eventType, String description,
                               String resourceType, Long resourceId, String errorMessage) {
        UserProfile user = userRepository.findById(userId).orElse(null);
        
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userName(user != null ? user.getFullName() : "Unknown")
                .eventType(eventType)
                .eventDescription(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .realmId(user != null ? user.getRealm().getId() : null)
                .requestId(UUID.randomUUID().toString())
                .result(AuditLog.RESULT_FAILURE)
                .errorMessage(errorMessage)
                .build();

        auditLog = auditLogRepository.save(auditLog);

        log.warn("Audit log (failure): {} by user {} - {}", eventType, userId, errorMessage);

        return auditLog;
    }

    /**
     * 记录登录日志
     */
    @Transactional
    public AuditLog logLogin(Long userId, String ipAddress, String userAgent, boolean success) {
        UserProfile user = userRepository.findById(userId).orElse(null);
        
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userName(user != null ? user.getFullName() : "Unknown")
                .eventType(AuditLog.TYPE_LOGIN)
                .eventDescription("User login")
                .resourceType(AuditLog.RESOURCE_USER)
                .resourceId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .realmId(user != null ? user.getRealm().getId() : null)
                .requestId(UUID.randomUUID().toString())
                .result(success ? AuditLog.RESULT_SUCCESS : AuditLog.RESULT_FAILURE)
                .build();

        auditLog = auditLogRepository.save(auditLog);

        log.info("Login audit log: user {} from {}", userId, ipAddress);

        return auditLog;
    }

    /**
     * 记录消息发送日志
     */
    @Transactional
    public AuditLog logMessageSend(Long userId, Long messageId, String contentPreview) {
        return log(userId, AuditLog.TYPE_SEND, "Send message: " + contentPreview,
                   AuditLog.RESOURCE_MESSAGE, messageId, null, null, null, null);
    }

    /**
     * 记录资源创建日志
     */
    @Transactional
    public AuditLog logCreate(Long userId, String resourceType, Long resourceId, Object newValue) {
        return log(userId, AuditLog.TYPE_CREATE, "Create " + resourceType,
                   resourceType, resourceId, null, newValue, null, null);
    }

    /**
     * 记录资源更新日志
     */
    @Transactional
    public AuditLog logUpdate(Long userId, String resourceType, Long resourceId, 
                              Object oldValue, Object newValue) {
        return log(userId, AuditLog.TYPE_UPDATE, "Update " + resourceType,
                   resourceType, resourceId, oldValue, newValue, null, null);
    }

    /**
     * 记录资源删除日志
     */
    @Transactional
    public AuditLog logDelete(Long userId, String resourceType, Long resourceId, Object oldValue) {
        return log(userId, AuditLog.TYPE_DELETE, "Delete " + resourceType,
                   resourceType, resourceId, oldValue, null, null, null);
    }

    /**
     * 获取用户操作日志
     */
    public List<AuditLog> getUserLogs(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    /**
     * 获取时间范围内的日志
     */
    public List<AuditLog> getLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimeRange(start, end);
    }

    /**
     * 获取特定资源的日志
     */
    public List<AuditLog> getResourceLogs(String resourceType, Long resourceId) {
        return auditLogRepository.findByResource(resourceType, resourceId);
    }

    /**
     * 获取失败日志
     */
    public List<AuditLog> getFailedLogs() {
        return auditLogRepository.findFailedLogs();
    }

    /**
     * 获取最近日志
     */
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findRecentLogs(limit);
    }

    /**
     * 搜索日志
     */
    public List<AuditLog> searchLogs(String query) {
        return auditLogRepository.searchByDescription(query);
    }

    /**
     * 统计用户操作次数
     */
    public Long countUserLogs(Long userId) {
        return auditLogRepository.countByUserId(userId);
    }

    /**
     * 统计失败操作次数
     */
    public Long countFailedLogs(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.countFailedSince(since);
    }

    /**
     * 按事件类型统计
     */
    public List<Object[]> countByEventType(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.countByEventTypeGrouped(since);
    }

    /**
     * 分页查询用户日志
     */
    public List<AuditLog> getUserLogsPaged(Long userId, int page, int size) {
        return auditLogRepository.findByUserIdPaged(userId, PageRequest.of(page, size));
    }

    /**
     * 转换为 DTO
     */
    public AuditLogDTO.AuditLogResponse toResponse(AuditLog log) {
        return AuditLogDTO.AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userName(log.getUserName())
                .eventType(log.getEventType())
                .eventDescription(log.getEventDescription())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .result(log.getResult())
                .errorMessage(log.getErrorMessage())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .realmId(log.getRealmId())
                .eventTime(log.getEventTime().toString())
                .requestId(log.getRequestId())
                .build();
    }

    /**
     * 获取审计日志摘要
     */
    public AuditLogDTO.AuditSummary getSummary(Long realmId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        LocalDateTime end = LocalDateTime.now();
        
        List<AuditLog> logs = auditLogRepository.findByTimeRange(since, end);
        
        long total = logs.size();
        long successCount = logs.stream().filter(l -> l.getResult().equals(AuditLog.RESULT_SUCCESS)).count();
        long failureCount = total - successCount;
        
        return AuditLogDTO.AuditSummary.builder()
                .realmId(realmId)
                .timeRangeStart(since.toString())
                .timeRangeEnd(end.toString())
                .totalOperations(total)
                .successCount(successCount)
                .failureCount(failureCount)
                .failureRate(total > 0 ? (double) failureCount / total * 100 : 0)
                .build();
    }

    /**
     * 获取请求追踪链
     */
    public List<AuditLog> getRequestChain(String requestId) {
        return auditLogRepository.findByRequestId(requestId);
    }
}
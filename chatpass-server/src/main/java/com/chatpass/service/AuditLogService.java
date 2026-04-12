package com.chatpass.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chatpass.dto.AuditLogDTO;
import com.chatpass.entity.AuditLog;
import com.chatpass.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审计日志服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    
    public static final String RESOURCE_USER = "user";
    public static final String RESOURCE_MESSAGE = "message";
    public static final String RESOURCE_STREAM = "stream";
    public static final String RESOURCE_BOT = "bot";
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 记录操作日志
     */
    @Transactional
    public void logEvent(Long actorId, String eventType, Long realmId, 
                         String objectType, Long objectId, Map<String, Object> extraData,
                         String ipAddress, String userAgent, String result, String errorMessage) {
        
        AuditLog auditLog = AuditLog.builder()
                .actorId(actorId)
                .eventType(eventType)
                .eventTime(LocalDateTime.now())
                .realmId(realmId)
                .objectType(objectType)
                .objectId(objectId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result(result != null ? result : "success")
                .errorMessage(errorMessage)
                .build();
        
        // 转换extraData为JSON字符串
        if (extraData != null && !extraData.isEmpty()) {
            try {
                auditLog.setExtraData(objectMapper.writeValueAsString(extraData));
            } catch (Exception e) {
                log.warn("Failed to serialize extra data: {}", e.getMessage());
            }
        }
        
        auditLogRepository.save(auditLog);
        log.info("审计日志: {} (actor={}, realm={})", eventType, actorId, realmId);
    }
    
    /**
     * 记录成功操作
     */
    @Transactional
    public void logSuccess(Long actorId, String eventType, Long realmId,
                           String objectType, Long objectId, Map<String, Object> extraData,
                           String ipAddress) {
        logEvent(actorId, eventType, realmId, objectType, objectId, extraData, 
                 ipAddress, null, "success", null);
    }
    
    /**
     * 记录失败操作
     */
    @Transactional
    public void logFailure(Long actorId, String eventType, Long realmId,
                           String objectType, Long objectId, String errorMessage,
                           String ipAddress) {
        logEvent(actorId, eventType, realmId, objectType, objectId, null,
                 ipAddress, null, "failure", errorMessage);
    }
    
    /**
     * 获取组织的审计日志
     */
    public List<AuditLogDTO> getLogsByRealm(Long realmId, int limit) {
        return auditLogRepository.findRecentLogs(realmId, limit)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户操作日志
     */
    public List<AuditLogDTO> getLogsByUser(Long actorId) {
        return auditLogRepository.findByActorIdOrderByEventTimeDesc(actorId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取特定事件类型的日志
     */
    public List<AuditLogDTO> getLogsByType(Long realmId, String eventType) {
        return auditLogRepository.findByRealmIdAndEventTypeOrderByEventTimeDesc(realmId, eventType)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取时间范围内的日志
     */
    public List<AuditLogDTO> getLogsByTimeRange(Long realmId, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByRealmIdAndTimeRange(realmId, start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取对象的操作历史
     */
    public List<AuditLogDTO> getObjectHistory(String objectType, Long objectId) {
        return auditLogRepository.findByObjectTypeAndObjectIdOrderByEventTimeDesc(objectType, objectId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取日志统计
     */
    public Map<String, Object> getStats(Long realmId, LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalEvents = auditLogRepository.countByRealmIdSince(realmId, since);
        stats.put("total_events", totalEvents);
        
        // 常见事件类型统计
        String[] commonTypes = {"user_created", "message_sent", "stream_created", "user_login"};
        for (String type : commonTypes) {
            stats.put(type + "_count", auditLogRepository.countByRealmIdAndEventType(realmId, type));
        }
        
        return stats;
    }
    
    /**
     * 获取日志详情
     */
    public AuditLogDTO getLogById(Long logId) {
        return auditLogRepository.findById(logId)
                .map(this::toDTO)
                .orElse(null);
    }
    
    /**
     * 便捷方法：记录创建操作
     */
    @Transactional
    public void logCreate(Long actorId, String objectType, Long objectId, Object object) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("action", "create");
        extraData.put("object_type", objectType);
        
        try {
            extraData.put("object", objectMapper.writeValueAsString(object));
        } catch (Exception e) {
            log.warn("Failed to serialize object: {}", e.getMessage());
        }
        
        logSuccess(actorId, objectType + "_created", null, objectType, objectId, extraData, null);
    }
    
    /**
     * 便捷方法：记录更新操作
     */
    @Transactional
    public void logUpdate(Long actorId, String objectType, Long objectId, Object oldObject, Object newObject) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("action", "update");
        extraData.put("object_type", objectType);
        
        try {
            extraData.put("old_object", objectMapper.writeValueAsString(oldObject));
            extraData.put("new_object", objectMapper.writeValueAsString(newObject));
        } catch (Exception e) {
            log.warn("Failed to serialize objects: {}", e.getMessage());
        }
        
        logSuccess(actorId, objectType + "_updated", null, objectType, objectId, extraData, null);
    }
    
    /**
     * 便捷方法：记录删除操作
     */
    @Transactional
    public void logDelete(Long actorId, String objectType, Long objectId, Object object) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("action", "delete");
        extraData.put("object_type", objectType);
        
        try {
            extraData.put("object", objectMapper.writeValueAsString(object));
        } catch (Exception e) {
            log.warn("Failed to serialize object: {}", e.getMessage());
        }
        
        logSuccess(actorId, objectType + "_deleted", null, objectType, objectId, extraData, null);
    }
    
    private AuditLogDTO toDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .eventType(log.getEventType())
                .eventTime(log.getEventTime())
                .realmId(log.getRealmId())
                .objectType(log.getObjectType())
                .objectId(log.getObjectId())
                .extraData(log.getExtraData())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .result(log.getResult())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}

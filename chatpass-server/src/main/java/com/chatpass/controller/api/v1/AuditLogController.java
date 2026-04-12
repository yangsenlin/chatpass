package com.chatpass.controller.api.v1;

import com.chatpass.dto.AuditLogDTO;
import com.chatpass.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    /**
     * 获取组织的审计日志
     */
    @GetMapping("/realm/{realmId}/audit_logs")
    public ResponseEntity<List<AuditLogDTO>> getRealmLogs(
            @PathVariable Long realmId,
            @RequestParam(defaultValue = "100") int limit) {
        
        List<AuditLogDTO> logs = auditLogService.getLogsByRealm(realmId, limit);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取用户操作日志
     */
    @GetMapping("/users/{userId}/audit_logs")
    public ResponseEntity<List<AuditLogDTO>> getUserLogs(@PathVariable Long userId) {
        List<AuditLogDTO> logs = auditLogService.getLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取特定事件类型的日志
     */
    @GetMapping("/realm/{realmId}/audit_logs/type/{eventType}")
    public ResponseEntity<List<AuditLogDTO>> getLogsByType(
            @PathVariable Long realmId,
            @PathVariable String eventType) {
        
        List<AuditLogDTO> logs = auditLogService.getLogsByType(realmId, eventType);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取时间范围内的日志
     */
    @GetMapping("/realm/{realmId}/audit_logs/time_range")
    public ResponseEntity<List<AuditLogDTO>> getLogsByTimeRange(
            @PathVariable Long realmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<AuditLogDTO> logs = auditLogService.getLogsByTimeRange(realmId, start, end);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取对象的操作历史
     */
    @GetMapping("/audit_logs/object/{objectType}/{objectId}")
    public ResponseEntity<List<AuditLogDTO>> getObjectHistory(
            @PathVariable String objectType,
            @PathVariable Long objectId) {
        
        List<AuditLogDTO> logs = auditLogService.getObjectHistory(objectType, objectId);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取日志详情
     */
    @GetMapping("/audit_logs/{logId}")
    public ResponseEntity<AuditLogDTO> getLogById(@PathVariable Long logId) {
        AuditLogDTO log = auditLogService.getLogById(logId);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(log);
    }
    
    /**
     * 获取日志统计
     */
    @GetMapping("/realm/{realmId}/audit_logs/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable Long realmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        Map<String, Object> stats = auditLogService.getStats(realmId, since);
        return ResponseEntity.ok(stats);
    }
}

package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.AuditLogDTO;
import com.chatpass.entity.AuditLog;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AuditLog 控制器
 * 
 * 审计日志查询 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "审计日志 API")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final SecurityUtil securityUtil;

    @GetMapping("/audit/logs/user/{userId}")
    @Operation(summary = "获取用户操作日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getUserLogs(
            @PathVariable Long userId) {
        List<AuditLog> logs = auditLogService.getUserLogs(userId);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/recent")
    @Operation(summary = "获取最近日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getRecentLogs(
            @RequestParam(defaultValue = "100") int limit) {
        List<AuditLog> logs = auditLogService.getRecentLogs(limit);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/time-range")
    @Operation(summary = "按时间范围查询日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<AuditLog> logs = auditLogService.getLogsByTimeRange(start, end);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/resource/{resourceType}/{resourceId}")
    @Operation(summary = "获取资源操作日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getResourceLogs(
            @PathVariable String resourceType,
            @PathVariable Long resourceId) {
        List<AuditLog> logs = auditLogService.getResourceLogs(resourceType, resourceId);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/failed")
    @Operation(summary = "获取失败操作日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getFailedLogs() {
        List<AuditLog> logs = auditLogService.getFailedLogs();
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/search")
    @Operation(summary = "搜索日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> searchLogs(
            @RequestParam String query) {
        List<AuditLog> logs = auditLogService.searchLogs(query);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/user/{userId}/paged")
    @Operation(summary = "分页获取用户日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getUserLogsPaged(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<AuditLog> logs = auditLogService.getUserLogsPaged(userId, page, size);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/summary")
    @Operation(summary = "获取审计日志摘要")
    public ResponseEntity<ApiResponse<AuditLogDTO.AuditSummary>> getSummary(
            @RequestParam(defaultValue = "24") int hours) {
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        AuditLogDTO.AuditSummary summary = auditLogService.getSummary(realmId, hours);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/audit/logs/count/user/{userId}")
    @Operation(summary = "统计用户操作次数")
    public ResponseEntity<ApiResponse<Long>> countUserLogs(@PathVariable Long userId) {
        Long count = auditLogService.countUserLogs(userId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/audit/logs/count/failed")
    @Operation(summary = "统计失败操作次数")
    public ResponseEntity<ApiResponse<Long>> countFailedLogs(
            @RequestParam(defaultValue = "24") int hours) {
        Long count = auditLogService.countFailedLogs(hours);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/audit/logs/count/by-type")
    @Operation(summary = "按事件类型统计")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.EventTypeCount>>> countByEventType(
            @RequestParam(defaultValue = "24") int hours) {
        List<Object[]> counts = auditLogService.countByEventType(hours);
        
        List<AuditLogDTO.EventTypeCount> response = counts.stream()
                .map(obj -> AuditLogDTO.EventTypeCount.builder()
                        .eventType((String) obj[0])
                        .count((Long) obj[1])
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/request/{requestId}")
    @Operation(summary = "获取请求追踪链")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getRequestChain(
            @PathVariable String requestId) {
        List<AuditLog> logs = auditLogService.getRequestChain(requestId);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/my")
    @Operation(summary = "获取我的操作日志")
    public ResponseEntity<ApiResponse<List<AuditLogDTO.AuditLogResponse>>> getMyLogs() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<AuditLog> logs = auditLogService.getUserLogs(userId);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/logs/my/paged")
    @Operation(summary = "分页获取我的日志")
    public ResponseEntity<ApiResponse<AuditLogDTO.AuditLogListResponse>> getMyLogsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<AuditLog> logs = auditLogService.getUserLogsPaged(userId, page, size);
        Long total = auditLogService.countUserLogs(userId);
        
        List<AuditLogDTO.AuditLogResponse> response = logs.stream()
                .map(auditLogService::toResponse)
                .collect(Collectors.toList());
        
        AuditLogDTO.AuditLogListResponse listResponse = AuditLogDTO.AuditLogListResponse.builder()
                .logs(response)
                .total(total)
                .page(page)
                .size(size)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(listResponse));
    }
}
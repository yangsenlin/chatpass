package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.AnalyticsDTO;
import com.chatpass.entity.AnalyticsReport;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analytics 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "数据分析 API")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtil securityUtil;

    @PostMapping("/analytics/reports")
    @Operation(summary = "生成报告")
    public ResponseEntity<ApiResponse<AnalyticsDTO.ReportResponse>> generateReport(
            @RequestBody AnalyticsDTO.GenerateReportRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        LocalDateTime start = request.getStartTime() != null ? 
                LocalDateTime.parse(request.getStartTime()) : LocalDateTime.now().minusDays(7);
        LocalDateTime end = request.getEndTime() != null ? 
                LocalDateTime.parse(request.getEndTime()) : LocalDateTime.now();
        
        AnalyticsReport report;
        
        switch (request.getReportType()) {
            case AnalyticsReport.TYPE_USER_ACTIVITY:
                report = analyticsService.generateUserActivityReport(realmId, userId, start, end, request.getPeriod());
                break;
            case AnalyticsReport.TYPE_STREAM_USAGE:
                report = analyticsService.generateStreamUsageReport(realmId, userId, start, end, request.getPeriod());
                break;
            case AnalyticsReport.TYPE_MESSAGE_STATS:
                report = analyticsService.generateMessageStatsReport(realmId, userId, start, end, request.getPeriod());
                break;
            case AnalyticsReport.TYPE_REACTION_STATS:
                report = analyticsService.generateReactionStatsReport(realmId, userId, start, end, request.getPeriod());
                break;
            default:
                throw new IllegalArgumentException("未知的报告类型");
        }
        
        return ResponseEntity.ok(ApiResponse.success(analyticsService.toResponse(report)));
    }

    @GetMapping("/analytics/reports/{reportId}")
    @Operation(summary = "获取报告详情")
    public ResponseEntity<ApiResponse<AnalyticsDTO.ReportResponse>> getReport(
            @PathVariable Long reportId) {
        AnalyticsReport report = analyticsService.getReport(reportId);
        
        return ResponseEntity.ok(ApiResponse.success(analyticsService.toResponse(report)));
    }

    @GetMapping("/analytics/reports")
    @Operation(summary = "获取报告列表")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO.ReportResponse>>> getReports() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<AnalyticsReport> reports = analyticsService.getRealmReports(realmId);
        
        List<AnalyticsDTO.ReportResponse> response = reports.stream()
                .map(analyticsService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/reports/type/{reportType}")
    @Operation(summary = "获取特定类型报告")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO.ReportResponse>>> getReportsByType(
            @PathVariable String reportType) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<AnalyticsReport> reports = analyticsService.getReportsByType(realmId, reportType);
        
        List<AnalyticsDTO.ReportResponse> response = reports.stream()
                .map(analyticsService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/reports/latest")
    @Operation(summary = "获取最新报告")
    public ResponseEntity<ApiResponse<AnalyticsDTO.ReportResponse>> getLatestReport(
            @RequestParam String reportType, @RequestParam String period) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        AnalyticsReport report = analyticsService.getLatestReport(realmId, reportType, period)
                .orElse(null);
        
        if (report == null) {
            return ResponseEntity.ok(ApiResponse.error("NOT_FOUND", "报告不存在"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(analyticsService.toResponse(report)));
    }

    @GetMapping("/analytics/quick-stats")
    @Operation(summary = "获取快速统计")
    public ResponseEntity<ApiResponse<AnalyticsDTO.QuickStats>> getQuickStats() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        AnalyticsDTO.QuickStats stats = analyticsService.getQuickStats(realmId);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/analytics/reports/old")
    @Operation(summary = "删除旧报告")
    public ResponseEntity<ApiResponse<Void>> deleteOldReports(
            @RequestParam String before) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        LocalDateTime beforeTime = LocalDateTime.parse(before);
        
        analyticsService.deleteOldReports(realmId, beforeTime);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
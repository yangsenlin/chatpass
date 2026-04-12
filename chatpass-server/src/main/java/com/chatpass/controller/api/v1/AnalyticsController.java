package com.chatpass.controller.api.v1;

import com.chatpass.dto.AnalyticsDTO;
import com.chatpass.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据分析控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    /**
     * 收集消息统计
     */
    @PostMapping("/realm/{realmId}/analytics/message_stats")
    public ResponseEntity<AnalyticsDTO.MessageStats> collectMessageStats(@PathVariable Long realmId) {
        AnalyticsDTO.MessageStats stats = analyticsService.collectMessageStats(realmId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 收集用户活跃度
     */
    @PostMapping("/realm/{realmId}/analytics/user_activity")
    public ResponseEntity<AnalyticsDTO.UserActivityStats> collectUserActivity(@PathVariable Long realmId) {
        AnalyticsDTO.UserActivityStats stats = analyticsService.collectUserActivity(realmId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 收集Stream使用统计
     */
    @PostMapping("/realm/{realmId}/analytics/stream_usage")
    public ResponseEntity<AnalyticsDTO.StreamUsageStats> collectStreamUsage(@PathVariable Long realmId) {
        AnalyticsDTO.StreamUsageStats stats = analyticsService.collectStreamUsage(realmId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 生成完整报告
     */
    @GetMapping("/realm/{realmId}/analytics/report")
    public ResponseEntity<AnalyticsDTO.Report> generateReport(@PathVariable Long realmId) {
        AnalyticsDTO.Report report = analyticsService.generateReport(realmId);
        return ResponseEntity.ok(report);
    }
    
    /**
     * 获取历史数据
     */
    @GetMapping("/realm/{realmId}/analytics/history")
    public ResponseEntity<List<AnalyticsDTO.DataPoint>> getHistoricalData(
            @PathVariable Long realmId,
            @RequestParam String dataType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<AnalyticsDTO.DataPoint> data = analyticsService.getHistoricalData(realmId, dataType, start, end);
        return ResponseEntity.ok(data);
    }
    
    /**
     * 获取最新数据
     */
    @GetMapping("/realm/{realmId}/analytics/latest")
    public ResponseEntity<AnalyticsDTO.DataPoint> getLatestData(
            @PathVariable Long realmId,
            @RequestParam String dataType) {
        
        return analyticsService.getLatestData(realmId, dataType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取数据汇总
     */
    @GetMapping("/realm/{realmId}/analytics/summary")
    public ResponseEntity<AnalyticsDTO.Summary> getSummary(
            @PathVariable Long realmId,
            @RequestParam String dataType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        AnalyticsDTO.Summary summary = analyticsService.getSummary(realmId, dataType, since);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * 获取平均值
     */
    @GetMapping("/realm/{realmId}/analytics/average")
    public ResponseEntity<Double> getAverage(
            @PathVariable Long realmId,
            @RequestParam String dataType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        Double average = analyticsService.getAverage(realmId, dataType, start, end);
        return ResponseEntity.ok(average);
    }
    
    /**
     * 获取可用数据类型
     */
    @GetMapping("/realm/{realmId}/analytics/types")
    public ResponseEntity<List<String>> getAvailableDataTypes(@PathVariable Long realmId) {
        List<String> types = analyticsService.getAvailableDataTypes(realmId);
        return ResponseEntity.ok(types);
    }
    
    /**
     * 手动记录数据
     */
    @PostMapping("/realm/{realmId}/analytics/record")
    public ResponseEntity<Void> recordMetric(
            @PathVariable Long realmId,
            @RequestParam String dataType,
            @RequestParam Long metricValue,
            @RequestParam(required = false) String period) {
        
        analyticsService.recordMetric(realmId, dataType, metricValue, period, null);
        return ResponseEntity.ok().build();
    }
}

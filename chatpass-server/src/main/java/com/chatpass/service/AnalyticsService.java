package com.chatpass.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chatpass.dto.AnalyticsDTO;
import com.chatpass.entity.AnalyticsData;
import com.chatpass.repository.AnalyticsDataRepository;
import com.chatpass.repository.MessageRepository;
import com.chatpass.repository.UserProfileRepository;
import com.chatpass.repository.StreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据分析服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    
    private final AnalyticsDataRepository analyticsRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final StreamRepository streamRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 记录统计数据
     */
    @Transactional
    public void recordMetric(Long realmId, String dataType, Long metricValue, 
                              String period, Map<String, Object> details) {
        
        AnalyticsData data = AnalyticsData.builder()
                .realmId(realmId)
                .dataType(dataType)
                .metricValue(metricValue)
                .period(period)
                .timestamp(LocalDateTime.now())
                .build();
        
        if (details != null) {
            try {
                data.setDetails(objectMapper.writeValueAsString(details));
            } catch (Exception e) {
                log.warn("Failed to serialize details: {}", e.getMessage());
            }
        }
        
        analyticsRepository.save(data);
        log.info("记录统计数据: realmId={}, type={}, value={}", realmId, dataType, metricValue);
    }
    
    /**
     * 收集消息统计
     */
    @Transactional
    public AnalyticsDTO.MessageStats collectMessageStats(Long realmId) {
        Long totalMessages = messageRepository.countByRealmId(realmId);
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        
        Map<String, Object> details = new HashMap<>();
        details.put("total_messages", totalMessages);
        details.put("period", "weekly");
        
        AnalyticsDTO.MessageStats stats = AnalyticsDTO.MessageStats.builder()
                .realmId(realmId)
                .totalMessages(totalMessages)
                .period("weekly")
                .details(details)
                .build();
        
        // 记录统计数据
        recordMetric(realmId, "message_count", totalMessages, "daily", details);
        
        return stats;
    }
    
    /**
     * 收集用户活跃度
     */
    @Transactional
    public AnalyticsDTO.UserActivityStats collectUserActivity(Long realmId) {
        long activeUsers = userRepository.countByRealmId(realmId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("active_users", activeUsers);
        
        AnalyticsDTO.UserActivityStats stats = AnalyticsDTO.UserActivityStats.builder()
                .realmId(realmId)
                .activeUsers(activeUsers)
                .period("daily")
                .details(details)
                .build();
        
        recordMetric(realmId, "user_activity", activeUsers, "daily", details);
        
        return stats;
    }
    
    /**
     * 收集Stream使用统计
     */
    @Transactional
    public AnalyticsDTO.StreamUsageStats collectStreamUsage(Long realmId) {
        long streamCount = streamRepository.count();
        
        Map<String, Object> details = new HashMap<>();
        details.put("stream_count", streamCount);
        
        AnalyticsDTO.StreamUsageStats stats = AnalyticsDTO.StreamUsageStats.builder()
                .realmId(realmId)
                .streamCount(streamCount)
                .period("monthly")
                .details(details)
                .build();
        
        recordMetric(realmId, "stream_usage", streamCount, "monthly", details);
        
        return stats;
    }
    
    /**
     * 获取历史数据
     */
    public List<AnalyticsDTO.DataPoint> getHistoricalData(Long realmId, String dataType, 
                                                            LocalDateTime start, LocalDateTime end) {
        
        return analyticsRepository.findByTypeAndTimeRange(realmId, dataType, start, end)
                .stream()
                .map(this::toDataPoint)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取最新数据
     */
    public Optional<AnalyticsDTO.DataPoint> getLatestData(Long realmId, String dataType) {
        return analyticsRepository.findFirstByRealmIdAndDataTypeOrderByTimestampDesc(realmId, dataType)
                .map(this::toDataPoint);
    }
    
    /**
     * 获取数据汇总
     */
    public AnalyticsDTO.Summary getSummary(Long realmId, String dataType, LocalDateTime since) {
        Long total = analyticsRepository.sumMetricValueByTypeSince(realmId, dataType, since);
        
        return AnalyticsDTO.Summary.builder()
                .realmId(realmId)
                .dataType(dataType)
                .totalValue(total != null ? total : 0L)
                .since(since)
                .build();
    }
    
    /**
     * 获取平均值
     */
    public Double getAverage(Long realmId, String dataType, LocalDateTime start, LocalDateTime end) {
        return analyticsRepository.avgMetricValueByTypeAndTimeRange(realmId, dataType, start, end);
    }
    
    /**
     * 生成完整报告
     */
    public AnalyticsDTO.Report generateReport(Long realmId) {
        AnalyticsDTO.MessageStats messageStats = collectMessageStats(realmId);
        AnalyticsDTO.UserActivityStats userStats = collectUserActivity(realmId);
        AnalyticsDTO.StreamUsageStats streamStats = collectStreamUsage(realmId);
        
        return AnalyticsDTO.Report.builder()
                .realmId(realmId)
                .messageStats(messageStats)
                .userActivityStats(userStats)
                .streamUsageStats(streamStats)
                .generatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 获取所有数据类型
     */
    public List<String> getAvailableDataTypes(Long realmId) {
        return analyticsRepository.findByRealmIdOrderByTimestampDesc(realmId)
                .stream()
                .map(AnalyticsData::getDataType)
                .distinct()
                .collect(Collectors.toList());
    }
    
    private AnalyticsDTO.DataPoint toDataPoint(AnalyticsData data) {
        Map<String, Object> details = null;
        if (data.getDetails() != null) {
            try {
                details = objectMapper.readValue(data.getDetails(), Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse details: {}", e.getMessage());
            }
        }
        
        return AnalyticsDTO.DataPoint.builder()
                .id(data.getId())
                .realmId(data.getRealmId())
                .dataType(data.getDataType())
                .period(data.getPeriod())
                .timestamp(data.getTimestamp())
                .metricValue(data.getMetricValue())
                .details(details)
                .build();
    }
}

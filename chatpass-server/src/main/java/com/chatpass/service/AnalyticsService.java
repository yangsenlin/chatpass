package com.chatpass.service;

import com.chatpass.dto.AnalyticsDTO;
import com.chatpass.entity.AnalyticsReport;
import com.chatpass.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * AnalyticsService
 * 
 * 数据分析服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsReportRepository reportRepository;
    private final MessageRepository messageRepository;
    private final StreamRepository streamRepository;
    private final UserProfileRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    /**
     * 生成用户活跃度报告
     */
    @Transactional
    public AnalyticsReport generateUserActivityReport(Long realmId, Long creatorId, 
                                                       LocalDateTime start, LocalDateTime end,
                                                       String period) {
        try {
            // 统计用户活跃度
            Map<String, Object> data = new HashMap<>();
            
            // 消息发送统计
            Long totalMessages = messageRepository.countByRealmId(realmId);
            data.put("totalMessages", totalMessages);
            
            // 活跃用户统计
            Long activeUsers = userRepository.countByRealmId(realmId);
            data.put("activeUsers", activeUsers);
            
            // 新用户统计
            Long newUsers = userRepository.countNewUsers(realmId, start, end);
            data.put("newUsers", newUsers);
            
            // 消息发送者统计
            List<Object[]> topSenders = messageRepository.findTopSenders(realmId, start, end, 10);
            data.put("topSenders", topSenders);
            
            String dataJson = objectMapper.writeValueAsString(data);
            
            // 摘要
            String summary = String.format("活跃用户: %d, 总消息: %d, 新用户: %d", 
                    activeUsers, totalMessages, newUsers);
            
            AnalyticsReport report = AnalyticsReport.builder()
                    .realmId(realmId)
                    .reportType(AnalyticsReport.TYPE_USER_ACTIVITY)
                    .period(period)
                    .startTime(start)
                    .endTime(end)
                    .reportData(dataJson)
                    .summary(summary)
                    .creatorId(creatorId)
                    .build();

            report = reportRepository.save(report);

            log.info("User activity report generated for realm {}", realmId);

            return report;
        } catch (Exception e) {
            log.error("Failed to generate user activity report: {}", e.getMessage());
            throw new RuntimeException("生成报告失败", e);
        }
    }

    /**
     * 生成 Stream 使用统计报告
     */
    @Transactional
    public AnalyticsReport generateStreamUsageReport(Long realmId, Long creatorId,
                                                      LocalDateTime start, LocalDateTime end,
                                                      String period) {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // Stream 统计
            Long totalStreams = streamRepository.countByRealmId(realmId);
            data.put("totalStreams", totalStreams);
            
            // 活跃 Stream
            Long activeStreams = streamRepository.countActiveStreams(realmId);
            data.put("activeStreams", activeStreams);
            
            // Stream 消息量统计
            List<Object[]> topStreams = messageRepository.findTopStreams(realmId, start, end, 10);
            data.put("topStreams", topStreams);
            
            String dataJson = objectMapper.writeValueAsString(data);
            
            String summary = String.format("总 Stream: %d, 活跃 Stream: %d", 
                    totalStreams, activeStreams);
            
            AnalyticsReport report = AnalyticsReport.builder()
                    .realmId(realmId)
                    .reportType(AnalyticsReport.TYPE_STREAM_USAGE)
                    .period(period)
                    .startTime(start)
                    .endTime(end)
                    .reportData(dataJson)
                    .summary(summary)
                    .creatorId(creatorId)
                    .build();

            report = reportRepository.save(report);

            log.info("Stream usage report generated for realm {}", realmId);

            return report;
        } catch (Exception e) {
            log.error("Failed to generate stream usage report: {}", e.getMessage());
            throw new RuntimeException("生成报告失败", e);
        }
    }

    /**
     * 生成消息统计报告
     */
    @Transactional
    public AnalyticsReport generateMessageStatsReport(Long realmId, Long creatorId,
                                                       LocalDateTime start, LocalDateTime end,
                                                       String period) {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // 消息统计
            Long totalMessages = messageRepository.countByRealmIdAndTimeRange(realmId, start, end);
            data.put("totalMessages", totalMessages);
            
            // 平均消息长度
            Double avgLength = messageRepository.avgMessageLength(realmId, start, end);
            data.put("avgMessageLength", avgLength);
            
            // 消息类型分布
            Map<String, Long> typeDistribution = new HashMap<>();
            typeDistribution.put("stream", messageRepository.countStreamMessages(realmId, start, end));
            typeDistribution.put("private", messageRepository.countPrivateMessages(realmId, start, end));
            data.put("typeDistribution", typeDistribution);
            
            // 小时分布
            List<Object[]> hourlyDistribution = messageRepository.hourlyDistribution(realmId, start, end);
            data.put("hourlyDistribution", hourlyDistribution);
            
            String dataJson = objectMapper.writeValueAsString(data);
            
            String summary = String.format("总消息: %d, 平均长度: %.2f", 
                    totalMessages, avgLength);
            
            AnalyticsReport report = AnalyticsReport.builder()
                    .realmId(realmId)
                    .reportType(AnalyticsReport.TYPE_MESSAGE_STATS)
                    .period(period)
                    .startTime(start)
                    .endTime(end)
                    .reportData(dataJson)
                    .summary(summary)
                    .creatorId(creatorId)
                    .build();

            report = reportRepository.save(report);

            log.info("Message stats report generated for realm {}", realmId);

            return report;
        } catch (Exception e) {
            log.error("Failed to generate message stats report: {}", e.getMessage());
            throw new RuntimeException("生成报告失败", e);
        }
    }

    /**
     * 生成表情统计报告
     */
    @Transactional
    public AnalyticsReport generateReactionStatsReport(Long realmId, Long creatorId,
                                                        LocalDateTime start, LocalDateTime end,
                                                        String period) {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // 表情总数
            Long totalReactions = reactionRepository.countByRealmId(realmId);
            data.put("totalReactions", totalReactions);
            
            // 热门表情
            List<Object[]> topReactions = reactionRepository.findTopReactions(realmId, start, end, 10);
            data.put("topReactions", topReactions);
            
            String dataJson = objectMapper.writeValueAsString(data);
            
            String summary = String.format("总表情: %d", totalReactions);
            
            AnalyticsReport report = AnalyticsReport.builder()
                    .realmId(realmId)
                    .reportType(AnalyticsReport.TYPE_REACTION_STATS)
                    .period(period)
                    .startTime(start)
                    .endTime(end)
                    .reportData(dataJson)
                    .summary(summary)
                    .creatorId(creatorId)
                    .build();

            report = reportRepository.save(report);

            log.info("Reaction stats report generated for realm {}", realmId);

            return report;
        } catch (Exception e) {
            log.error("Failed to generate reaction stats report: {}", e.getMessage());
            throw new RuntimeException("生成报告失败", e);
        }
    }

    /**
     * 获取报告
     */
    public AnalyticsReport getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("报告不存在"));
    }

    /**
     * 获取 Realm 报告列表
     */
    public List<AnalyticsReport> getRealmReports(Long realmId) {
        return reportRepository.findByRealmId(realmId);
    }

    /**
     * 获取特定类型报告
     */
    public List<AnalyticsReport> getReportsByType(Long realmId, String reportType) {
        return reportRepository.findByRealmIdAndType(realmId, reportType);
    }

    /**
     * 获取最新报告
     */
    public Optional<AnalyticsReport> getLatestReport(Long realmId, String reportType, String period) {
        return reportRepository.findLatestReport(realmId, reportType, period);
    }

    /**
     * 获取时间范围报告
     */
    public List<AnalyticsReport> getReportsByTimeRange(Long realmId, LocalDateTime start, LocalDateTime end) {
        return reportRepository.findByTimeRange(realmId, start, end);
    }

    /**
     * 删除旧报告
     */
    @Transactional
    public void deleteOldReports(Long realmId, LocalDateTime before) {
        reportRepository.deleteOldReports(realmId, before);
        log.info("Old reports deleted for realm {}", realmId);
    }

    /**
     * 转换为 DTO
     */
    public AnalyticsDTO.ReportResponse toResponse(AnalyticsReport report) {
        return AnalyticsDTO.ReportResponse.builder()
                .id(report.getId())
                .realmId(report.getRealmId())
                .reportType(report.getReportType())
                .period(report.getPeriod())
                .startTime(report.getStartTime() != null ? report.getStartTime().toString() : null)
                .endTime(report.getEndTime() != null ? report.getEndTime().toString() : null)
                .reportData(report.getReportData())
                .summary(report.getSummary())
                .creatorId(report.getCreatorId())
                .reportTime(report.getReportTime().toString())
                .build();
    }

    /**
     * 快速统计
     */
    public AnalyticsDTO.QuickStats getQuickStats(Long realmId) {
        Long totalMessages = messageRepository.countByRealmId(realmId);
        Long totalUsers = userRepository.countByRealmId(realmId);
        Long totalStreams = streamRepository.countByRealmId(realmId);
        Long totalReactions = reactionRepository.countByRealmId(realmId);

        return AnalyticsDTO.QuickStats.builder()
                .totalMessages(totalMessages)
                .totalUsers(totalUsers)
                .totalStreams(totalStreams)
                .totalReactions(totalReactions)
                .build();
    }
}
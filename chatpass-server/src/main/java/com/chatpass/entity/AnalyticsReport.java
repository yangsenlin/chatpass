package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AnalyticsReport 实体 - 数据分析报告
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "analytics_reports", indexes = {
    @Index(name = "idx_report_realm", columnList = "realm_id"),
    @Index(name = "idx_report_type", columnList = "report_type"),
    @Index(name = "idx_report_time", columnList = "report_time")
})
public class AnalyticsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Realm
    @Column(name = "realm_id", nullable = false)
    private Long realmId;

    // 报告类型: USER_ACTIVITY、STREAM_USAGE、MESSAGE_STATS、REACTION_STATS
    @Column(name = "report_type", nullable = false, length = 30)
    private String reportType;

    // 报告周期: DAILY、WEEKLY、MONTHLY
    @Column(name = "period", length = 20)
    @Builder.Default
    private String period = "DAILY";

    // 开始时间
    @Column(name = "start_time")
    private LocalDateTime startTime;

    // 结束时间
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // 报告数据（JSON）
    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;

    // 摘要（JSON）
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // 创建者
    @Column(name = "creator_id")
    private Long creatorId;

    // 报告时间
    @CreationTimestamp
    @Column(name = "report_time", nullable = false)
    private LocalDateTime reportTime;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // 类型常量
    public static final String TYPE_USER_ACTIVITY = "USER_ACTIVITY";
    public static final String TYPE_STREAM_USAGE = "STREAM_USAGE";
    public static final String TYPE_MESSAGE_STATS = "MESSAGE_STATS";
    public static final String TYPE_REACTION_STATS = "REACTION_STATS";

    // 周期常量
    public static final String PERIOD_DAILY = "DAILY";
    public static final String PERIOD_WEEKLY = "WEEKLY";
    public static final String PERIOD_MONTHLY = "MONTHLY";
}
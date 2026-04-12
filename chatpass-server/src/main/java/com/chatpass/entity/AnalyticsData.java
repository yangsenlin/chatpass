package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 数据分析实体
 * 用于存储各类统计和分析数据
 */
@Entity
@Table(name = "analytics_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id", nullable = false)
    private Long realmId;
    
    /**
     * 数据类型
     */
    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType; // message_count, user_activity, stream_usage, topic_stats, etc.
    
    /**
     * 时间周期
     */
    @Column(name = "period", length = 20)
    private String period; // daily, weekly, monthly
    
    /**
     * 时间戳
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * 指标值
     */
    @Column(name = "metric_value")
    private Long metricValue;
    
    /**
     * 详细数据（JSON格式）
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

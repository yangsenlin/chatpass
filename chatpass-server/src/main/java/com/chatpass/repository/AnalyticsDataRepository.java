package com.chatpass.repository;

import com.chatpass.entity.AnalyticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据分析仓库
 */
@Repository
public interface AnalyticsDataRepository extends JpaRepository<AnalyticsData, Long> {
    
    /**
     * 根据组织ID查找数据
     */
    List<AnalyticsData> findByRealmIdOrderByTimestampDesc(Long realmId);
    
    /**
     * 根据类型查找数据
     */
    List<AnalyticsData> findByRealmIdAndDataTypeOrderByTimestampDesc(Long realmId, String dataType);
    
    /**
     * 根据时间范围查找数据
     */
    @Query("SELECT a FROM AnalyticsData a WHERE a.realmId = :realmId AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AnalyticsData> findByRealmIdAndTimeRange(@Param("realmId") Long realmId, 
                                                    @Param("start") LocalDateTime start, 
                                                    @Param("end") LocalDateTime end);
    
    /**
     * 根据类型和时间范围查找
     */
    @Query("SELECT a FROM AnalyticsData a WHERE a.realmId = :realmId AND a.dataType = :dataType AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AnalyticsData> findByTypeAndTimeRange(@Param("realmId") Long realmId, 
                                                 @Param("dataType") String dataType,
                                                 @Param("start") LocalDateTime start, 
                                                 @Param("end") LocalDateTime end);
    
    /**
     * 获取最新数据
     */
    Optional<AnalyticsData> findFirstByRealmIdAndDataTypeOrderByTimestampDesc(Long realmId, String dataType);
    
    /**
     * 获取历史数据汇总
     */
    @Query("SELECT SUM(a.metricValue) FROM AnalyticsData a WHERE a.realmId = :realmId AND a.dataType = :dataType AND a.timestamp >= :start")
    Long sumMetricValueByTypeSince(@Param("realmId") Long realmId, 
                                    @Param("dataType") String dataType,
                                    @Param("start") LocalDateTime start);
    
    /**
     * 获取平均值
     */
    @Query("SELECT AVG(a.metricValue) FROM AnalyticsData a WHERE a.realmId = :realmId AND a.dataType = :dataType AND a.timestamp BETWEEN :start AND :end")
    Double avgMetricValueByTypeAndTimeRange(@Param("realmId") Long realmId,
                                             @Param("dataType") String dataType,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}

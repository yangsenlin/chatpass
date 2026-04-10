package com.chatpass.repository;

import com.chatpass.entity.AnalyticsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AnalyticsReportRepository
 */
@Repository
public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, Long> {

    /**
     * 查找 Realm 的报告
     */
    @Query("SELECT r FROM AnalyticsReport r WHERE r.realmId = :realmId ORDER BY r.reportTime DESC")
    List<AnalyticsReport> findByRealmId(@Param("realmId") Long realmId);

    /**
     * 查找特定类型的报告
     */
    @Query("SELECT r FROM AnalyticsReport r WHERE r.realmId = :realmId AND r.reportType = :reportType ORDER BY r.reportTime DESC")
    List<AnalyticsReport> findByRealmIdAndType(@Param("realmId") Long realmId, @Param("reportType") String reportType);

    /**
     * 查找时间范围内的报告
     */
    @Query("SELECT r FROM AnalyticsReport r WHERE r.realmId = :realmId AND r.reportTime BETWEEN :start AND :end ORDER BY r.reportTime DESC")
    List<AnalyticsReport> findByTimeRange(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 查找最新报告
     */
    @Query("SELECT r FROM AnalyticsReport r WHERE r.realmId = :realmId AND r.reportType = :reportType AND r.period = :period ORDER BY r.reportTime DESC LIMIT 1")
    Optional<AnalyticsReport> findLatestReport(@Param("realmId") Long realmId, @Param("reportType") String reportType, @Param("period") String period);

    /**
     * 统计报告数
     */
    @Query("SELECT COUNT(r) FROM AnalyticsReport r WHERE r.realmId = :realmId")
    Long countByRealmId(@Param("realmId") Long realmId);

    /**
     * 删除旧报告
     */
    @Query("DELETE FROM AnalyticsReport r WHERE r.realmId = :realmId AND r.reportTime < :before")
    void deleteOldReports(@Param("realmId") Long realmId, @Param("before") LocalDateTime before);
}
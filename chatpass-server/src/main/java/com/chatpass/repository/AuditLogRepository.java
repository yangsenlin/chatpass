package com.chatpass.repository;

import com.chatpass.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditLogRepository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 查找用户的所有操作日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId ORDER BY a.eventTime DESC")
    List<AuditLog> findByUserId(@Param("userId") Long userId);

    /**
     * 查找指定时间范围内的日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventTime BETWEEN :start AND :end ORDER BY a.eventTime DESC")
    List<AuditLog> findByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 查找特定类型的日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = :eventType ORDER BY a.eventTime DESC")
    List<AuditLog> findByEventType(@Param("eventType") String eventType);

    /**
     * 查找特定资源的日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId ORDER BY a.eventTime DESC")
    List<AuditLog> findByResource(@Param("resourceType") String resourceType, @Param("resourceId") Long resourceId);

    /**
     * 查找 Realm 的日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.realmId = :realmId ORDER BY a.eventTime DESC")
    List<AuditLog> findByRealmId(@Param("realmId") Long realmId);

    /**
     * 查找失败的操作日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.result = 'FAILURE' ORDER BY a.eventTime DESC")
    List<AuditLog> findFailedLogs();

    /**
     * 查找指定用户在时间范围内的日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.eventTime BETWEEN :start AND :end ORDER BY a.eventTime DESC")
    List<AuditLog> findByUserIdAndTimeRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 统计用户操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计特定类型操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.eventType = :eventType")
    Long countByEventType(@Param("eventType") String eventType);

    /**
     * 统计失败操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.result = 'FAILURE' AND a.eventTime >= :since")
    Long countFailedSince(@Param("since") LocalDateTime since);

    /**
     * 查找最近的日志
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.eventTime DESC LIMIT :limit")
    List<AuditLog> findRecentLogs(@Param("limit") int limit);

    /**
     * 搜索日志（按描述）
     */
    @Query("SELECT a FROM AuditLog a WHERE LOWER(a.eventDescription) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY a.eventTime DESC")
    List<AuditLog> searchByDescription(@Param("query") String query);

    /**
     * 查找特定 IP 的日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress ORDER BY a.eventTime DESC")
    List<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 分页查询用户日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId ORDER BY a.eventTime DESC")
    List<AuditLog> findByUserIdPaged(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找特定时间范围内的日志（分页）
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventTime BETWEEN :start AND :end ORDER BY a.eventTime DESC")
    List<AuditLog> findByTimeRangePaged(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    /**
     * 按事件类型分组统计
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditLog a WHERE a.eventTime >= :since GROUP BY a.eventType ORDER BY COUNT(a) DESC")
    List<Object[]> countByEventTypeGrouped(@Param("since") LocalDateTime since);

    /**
     * 查找特定请求 ID 的日志链
     */
    @Query("SELECT a FROM AuditLog a WHERE a.requestId = :requestId ORDER BY a.eventTime ASC")
    List<AuditLog> findByRequestId(@Param("requestId") String requestId);
}
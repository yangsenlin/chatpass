package com.chatpass.repository;

import com.chatpass.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志仓库
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * 根据组织ID查找日志
     */
    List<AuditLog> findByRealmIdOrderByEventTimeDesc(Long realmId);
    
    /**
     * 根据用户ID查找日志
     */
    List<AuditLog> findByActorIdOrderByEventTimeDesc(Long actorId);
    
    /**
     * 根据事件类型查找日志
     */
    List<AuditLog> findByEventTypeOrderByEventTimeDesc(String eventType);
    
    /**
     * 根据组织ID和事件类型查找日志
     */
    List<AuditLog> findByRealmIdAndEventTypeOrderByEventTimeDesc(Long realmId, String eventType);
    
    /**
     * 根据时间范围查找日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.realmId = :realmId AND a.eventTime BETWEEN :start AND :end ORDER BY a.eventTime DESC")
    List<AuditLog> findByRealmIdAndTimeRange(@Param("realmId") Long realmId, 
                                              @Param("start") LocalDateTime start, 
                                              @Param("end") LocalDateTime end);
    
    /**
     * 根据对象类型和ID查找日志
     */
    List<AuditLog> findByObjectTypeAndObjectIdOrderByEventTimeDesc(String objectType, Long objectId);
    
    /**
     * 统计事件数量
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.realmId = :realmId AND a.eventType = :eventType")
    long countByRealmIdAndEventType(@Param("realmId") Long realmId, @Param("eventType") String eventType);
    
    /**
     * 统计时间范围内的日志数量
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.realmId = :realmId AND a.eventTime >= :start")
    long countByRealmIdSince(@Param("realmId") Long realmId, @Param("start") LocalDateTime start);
    
    /**
     * 获取最近N条日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.realmId = :realmId ORDER BY a.eventTime DESC LIMIT :limit")
    List<AuditLog> findRecentLogs(@Param("realmId") Long realmId, @Param("limit") int limit);
}

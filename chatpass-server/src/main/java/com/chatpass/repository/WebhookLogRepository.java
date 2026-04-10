package com.chatpass.repository;

import com.chatpass.entity.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WebhookLogRepository
 */
@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {

    /**
     * 查找 Webhook 的调用日志
     */
    @Query("SELECT l FROM WebhookLog l WHERE l.webhook.id = :webhookId ORDER BY l.invokeTime DESC")
    List<WebhookLog> findByWebhookId(@Param("webhookId") Long webhookId);

    /**
     * 查找失败日志
     */
    @Query("SELECT l FROM WebhookLog l WHERE l.webhook.id = :webhookId AND l.result = 'FAILURE' ORDER BY l.invokeTime DESC")
    List<WebhookLog> findFailedLogs(@Param("webhookId") Long webhookId);

    /**
     * 查找时间范围内的日志
     */
    @Query("SELECT l FROM WebhookLog l WHERE l.webhook.id = :webhookId AND l.invokeTime BETWEEN :start AND :end ORDER BY l.invokeTime DESC")
    List<WebhookLog> findByTimeRange(@Param("webhookId") Long webhookId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 统计调用次数
     */
    @Query("SELECT COUNT(l) FROM WebhookLog l WHERE l.webhook.id = :webhookId")
    Long countByWebhookId(@Param("webhookId") Long webhookId);

    /**
     * 统计成功次数
     */
    @Query("SELECT COUNT(l) FROM WebhookLog l WHERE l.webhook.id = :webhookId AND l.result = 'SUCCESS'")
    Long countSuccessByWebhookId(@Param("webhookId") Long webhookId);

    /**
     * 统计失败次数
     */
    @Query("SELECT COUNT(l) FROM WebhookLog l WHERE l.webhook.id = :webhookId AND l.result != 'SUCCESS'")
    Long countFailureByWebhookId(@Param("webhookId") Long webhookId);

    /**
     * 统计平均响应时间
     */
    @Query("SELECT AVG(l.responseTimeMs) FROM WebhookLog l WHERE l.webhook.id = :webhookId AND l.result = 'SUCCESS'")
    Double avgResponseTime(@Param("webhookId") Long webhookId);

    /**
     * 分页查询
     */
    @Query("SELECT l FROM WebhookLog l WHERE l.webhook.id = :webhookId ORDER BY l.invokeTime DESC")
    List<WebhookLog> findByWebhookIdPaged(@Param("webhookId") Long webhookId, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找最近日志
     */
    @Query("SELECT l FROM WebhookLog l ORDER BY l.invokeTime DESC LIMIT :limit")
    List<WebhookLog> findRecentLogs(@Param("limit") int limit);
}
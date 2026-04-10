package com.chatpass.repository;

import com.chatpass.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WebhookRepository
 */
@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    /**
     * 查找所有者的 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.ownerId = :ownerId ORDER BY w.dateCreated DESC")
    List<Webhook> findByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 查找 Realm 的 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.realmId = :realmId AND w.isActive = true ORDER BY w.dateCreated DESC")
    List<Webhook> findByRealmId(@Param("realmId") Long realmId);

    /**
     * 通过 Key 查找 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.webhookKey = :webhookKey")
    Optional<Webhook> findByWebhookKey(@Param("webhookKey") String webhookKey);

    /**
     * 查找关联 Bot 的 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.botId = :botId ORDER BY w.dateCreated DESC")
    List<Webhook> findByBotId(@Param("botId") Long botId);

    /**
     * 查找活跃 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.isActive = true AND w.realmId = :realmId ORDER BY w.dateCreated DESC")
    List<Webhook> findActiveWebhooks(@Param("realmId") Long realmId);

    /**
     * 统计 Webhook 数量
     */
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.realmId = :realmId AND w.isActive = true")
    Long countByRealmId(@Param("realmId") Long realmId);

    /**
     * 搜索 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.realmId = :realmId AND LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY w.dateCreated DESC")
    List<Webhook> searchWebhooks(@Param("realmId") Long realmId, @Param("query") String query);

    /**
     * 验证 Webhook Key
     */
    @Query("SELECT COUNT(w) > 0 FROM Webhook w WHERE w.webhookKey = :webhookKey AND w.isActive = true")
    boolean validateWebhookKey(@Param("webhookKey") String webhookKey);

    /**
     * 查找最近调用的 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.lastInvoked >= :since ORDER BY w.lastInvoked DESC")
    List<Webhook> findRecentlyInvoked(@Param("since") java.time.LocalDateTime since);

    /**
     * 查找失败率高的 Webhook
     */
    @Query("SELECT w FROM Webhook w WHERE w.invokeCount > 10 AND (w.failureCount * 100.0 / w.invokeCount) > :threshold ORDER BY w.failureCount DESC")
    List<Webhook> findHighFailureRate(@Param("threshold") double threshold);
}
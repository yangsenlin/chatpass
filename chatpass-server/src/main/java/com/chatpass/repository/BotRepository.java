package com.chatpass.repository;

import com.chatpass.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BotRepository
 */
@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {

    /**
     * 查找所有者的所有 Bot
     */
    @Query("SELECT b FROM Bot b WHERE b.ownerId = :ownerId ORDER BY b.dateCreated DESC")
    List<Bot> findByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 查找 Realm 的所有 Bot
     */
    @Query("SELECT b FROM Bot b WHERE b.realmId = :realmId AND b.isActive = true ORDER BY b.dateCreated DESC")
    List<Bot> findByRealmId(@Param("realmId") Long realmId);

    /**
     * 通过 API Key 查找 Bot
     */
    @Query("SELECT b FROM Bot b WHERE b.apiKey = :apiKey")
    Optional<Bot> findByApiKey(@Param("apiKey") String apiKey);

    /**
     * 通过 Bot 用户 ID 查找
     */
    @Query("SELECT b FROM Bot b WHERE b.botUserId = :botUserId")
    Optional<Bot> findByBotUserId(@Param("botUserId") Long botUserId);

    /**
     * 通过名称查找
     */
    @Query("SELECT b FROM Bot b WHERE b.name = :name AND b.realmId = :realmId")
    Optional<Bot> findByNameAndRealmId(@Param("name") String name, @Param("realmId") Long realmId);

    /**
     * 查找活跃的 Bot
     */
    @Query("SELECT b FROM Bot b WHERE b.isActive = true ORDER BY b.dateCreated DESC")
    List<Bot> findActiveBots();

    /**
     * 查找特定类型的 Bot
     */
    @Query("SELECT b FROM Bot b WHERE b.botType = :botType AND b.realmId = :realmId ORDER BY b.dateCreated DESC")
    List<Bot> findByTypeAndRealmId(@Param("botType") String botType, @Param("realmId") Long realmId);

    /**
     * 统计 Realm 的 Bot 数量
     */
    @Query("SELECT COUNT(b) FROM Bot b WHERE b.realmId = :realmId AND b.isActive = true")
    Long countByRealmId(@Param("realmId") Long realmId);

    /**
     * 统计所有者的 Bot 数量
     */
    @Query("SELECT COUNT(b) FROM Bot b WHERE b.ownerId = :ownerId")
    Long countByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 检查名称是否存在
     */
    @Query("SELECT COUNT(b) > 0 FROM Bot b WHERE b.name = :name AND b.realmId = :realmId")
    boolean existsByName(@Param("name") String name, @Param("realmId") Long realmId);

    /**
     * 搜索 Bot
     */
    @Query("SELECT b FROM Bot b WHERE b.realmId = :realmId AND LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY b.dateCreated DESC")
    List<Bot> searchBots(@Param("realmId") Long realmId, @Param("query") String query);

    /**
     * 查找 Outgoing Bot
     */
    @Query("SELECT b FROM Bot b WHERE b.botType = 'OUTGOING' AND b.ownerId = :ownerId ORDER BY b.dateCreated DESC")
    List<Bot> findOutgoingBots(@Param("ownerId") Long ownerId);

    /**
     * 验证 API Key
     */
    @Query("SELECT COUNT(b) > 0 FROM Bot b WHERE b.apiKey = :apiKey AND b.isActive = true")
    boolean validateApiKey(@Param("apiKey") String apiKey);
}
package com.chatpass.repository;

import com.chatpass.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 机器人仓库
 */
@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
    
    /**
     * 根据组织ID查找所有Bot
     */
    List<Bot> findByRealmIdAndIsActiveTrueOrderByCreatedAtDesc(Long realmId);
    
    /**
     * 根据用户ID查找Bot
     */
    Optional<Bot> findByUserId(Long userId);
    
    /**
     * 根据API Key查找Bot
     */
    Optional<Bot> findByApiKey(String apiKey);
    
    /**
     * 根据所有者查找Bot
     */
    List<Bot> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    
    /**
     * 检查API Key是否已存在
     */
    boolean existsByApiKey(String apiKey);
    
    /**
     * 根据名称查找Bot
     */
    Optional<Bot> findByRealmIdAndName(Long realmId, String name);
    
    /**
     * 统计组织的Bot数量
     */
    long countByRealmIdAndIsActiveTrue(Long realmId);
    
    /**
     * 根据Bot类型查找
     */
    List<Bot> findByRealmIdAndBotType(Long realmId, String botType);
}

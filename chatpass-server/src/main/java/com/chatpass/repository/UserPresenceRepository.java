package com.chatpass.repository;

import com.chatpass.entity.UserPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户在线状态仓库
 */
@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, Long> {
    
    /**
     * 根据用户ID查找状态
     */
    Optional<UserPresence> findByUserId(Long userId);
    
    /**
     * 根据组织ID查找所有用户状态
     */
    List<UserPresence> findByRealmId(Long realmId);
    
    /**
     * 根据状态查找用户
     */
    List<UserPresence> findByStatus(String status);
    
    /**
     * 查找在线用户
     */
    @Query("SELECT up FROM UserPresence up WHERE up.realmId = :realmId AND up.status = 'online'")
    List<UserPresence> findOnlineUsers(@Param("realmId") Long realmId);
    
    /**
     * 查找活跃用户（最近活跃）
     */
    @Query("SELECT up FROM UserPresence up WHERE up.realmId = :realmId AND up.lastActive >= :since")
    List<UserPresence> findActiveUsers(@Param("realmId") Long realmId, @Param("since") LocalDateTime since);
    
    /**
     * 统计在线用户数量
     */
    @Query("SELECT COUNT(up) FROM UserPresence up WHERE up.realmId = :realmId AND up.status = 'online'")
    long countOnlineUsers(@Param("realmId") Long realmId);
    
    /**
     * 更新状态
     */
    @Modifying
    @Query("UPDATE UserPresence up SET up.status = :status, up.lastActive = :lastActive WHERE up.userId = :userId")
    void updateStatus(@Param("userId") Long userId, @Param("status") String status, @Param("lastActive") LocalDateTime lastActive);
    
    /**
     * 批量标记离线
     */
    @Modifying
    @Query("UPDATE UserPresence up SET up.status = 'offline', up.lastOffline = :lastOffline WHERE up.lastActive < :threshold AND up.status != 'offline'")
    void markInactiveOffline(@Param("threshold") LocalDateTime threshold, @Param("lastOffline") LocalDateTime lastOffline);
}

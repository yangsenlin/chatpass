package com.chatpass.repository;

import com.chatpass.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户状态仓库
 */
@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    
    /**
     * 根据用户ID查找状态
     */
    Optional<UserStatus> findByUserId(Long userId);
    
    /**
     * 根据组织ID查找所有状态
     */
    List<UserStatus> findByRealmIdOrderByCreatedAtDesc(Long realmId);
    
    /**
     * 查找有效状态
     */
    @Query("SELECT us FROM UserStatus us WHERE us.expiresAt IS NULL OR us.expiresAt > :now")
    List<UserStatus> findActiveStatuses(@Param("now") LocalDateTime now);
    
    /**
     * 查找组织的有效状态
     */
    @Query("SELECT us FROM UserStatus us WHERE us.realmId = :realmId AND (us.expiresAt IS NULL OR us.expiresAt > :now)")
    List<UserStatus> findActiveStatusesByRealm(@Param("realmId") Long realmId, @Param("now") LocalDateTime now);
    
    /**
     * 清除过期状态
     */
    @Modifying
    @Query("DELETE FROM UserStatus us WHERE us.expiresAt IS NOT NULL AND us.expiresAt < :now")
    void clearExpiredStatuses(@Param("now") LocalDateTime now);
    
    /**
     * 删除用户状态
     */
    @Modifying
    @Query("DELETE FROM UserStatus us WHERE us.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

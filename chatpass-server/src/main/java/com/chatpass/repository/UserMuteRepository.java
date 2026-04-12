package com.chatpass.repository;

import com.chatpass.entity.UserMute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户静音仓库
 */
@Repository
public interface UserMuteRepository extends JpaRepository<UserMute, Long> {
    
    /**
     * 根据用户ID查找所有静音用户
     */
    List<UserMute> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和被静音用户查找
     */
    Optional<UserMute> findByUserIdAndMutedUserId(Long userId, Long mutedUserId);
    
    /**
     * 检查是否已静音
     */
    boolean existsByUserIdAndMutedUserId(Long userId, Long mutedUserId);
    
    /**
     * 查找静音了某用户的所有用户
     */
    @Query("SELECT um.userId FROM UserMute um WHERE um.mutedUserId = :mutedUserId")
    List<Long> findUsersMutingUser(@Param("mutedUserId") Long mutedUserId);
    
    /**
     * 删除静音
     */
    @Modifying
    @Query("DELETE FROM UserMute um WHERE um.userId = :userId AND um.mutedUserId = :mutedUserId")
    void deleteByUserIdAndMutedUserId(@Param("userId") Long userId, @Param("mutedUserId") Long mutedUserId);
    
    /**
     * 统计用户静音数量
     */
    long countByUserId(Long userId);
}

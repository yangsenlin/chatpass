package com.chatpass.repository;

import com.chatpass.entity.MutedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MutedUser Repository
 */
@Repository
public interface MutedUserRepository extends JpaRepository<MutedUser, Long> {
    
    /**
     * 获取用户屏蔽的所有用户
     */
    List<MutedUser> findByUserId(Long userId);
    
    /**
     * 获取屏蔽用户的用户列表
     */
    @Query("SELECT mu.mutedUser.id FROM MutedUser mu WHERE mu.user.id = :userId")
    List<Long> findMutedUserIds(@Param("userId") Long userId);
    
    /**
     * 检查是否已屏蔽
     */
    @Query("SELECT CASE WHEN COUNT(mu) > 0 THEN true ELSE false END FROM MutedUser mu WHERE mu.user.id = :userId AND mu.mutedUser.id = :mutedUserId")
    boolean existsByUserIdAndMutedUserId(@Param("userId") Long userId, @Param("mutedUserId") Long mutedUserId);
    
    /**
     * 获取屏蔽记录
     */
    Optional<MutedUser> findByUserIdAndMutedUserId(Long userId, Long mutedUserId);
    
    /**
     * 删除屏蔽
     */
    @Modifying
    @Query("DELETE FROM MutedUser mu WHERE mu.user.id = :userId AND mu.mutedUser.id = :mutedUserId")
    void deleteByUserIdAndMutedUserId(@Param("userId") Long userId, @Param("mutedUserId") Long mutedUserId);
    
    /**
     * 删除用户的所有屏蔽记录（用户被删除时）
     */
    @Modifying
    @Query("DELETE FROM MutedUser mu WHERE mu.user.id = :userId OR mu.mutedUser.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
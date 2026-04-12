package com.chatpass.repository;

import com.chatpass.entity.NavigationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * NavigationView Repository
 */
@Repository
public interface NavigationViewRepository extends JpaRepository<NavigationView, Long> {
    
    /**
     * 获取用户的所有导航视图
     */
    List<NavigationView> findByUserId(Long userId);
    
    /**
     * 获取用户的固定视图
     */
    @Query("SELECT nv FROM NavigationView nv WHERE nv.user.id = :userId AND nv.isPinned = true ORDER BY nv.dateCreated")
    List<NavigationView> findPinnedViews(@Param("userId") Long userId);
    
    /**
     * 获取用户的隐藏视图
     */
    @Query("SELECT nv FROM NavigationView nv WHERE nv.user.id = :userId AND nv.isPinned = false")
    List<NavigationView> findHiddenViews(@Param("userId") Long userId);
    
    /**
     * 检查视图是否存在
     */
    @Query("SELECT CASE WHEN COUNT(nv) > 0 THEN true ELSE false END FROM NavigationView nv WHERE nv.user.id = :userId AND nv.fragment = :fragment")
    boolean existsByUserIdAndFragment(@Param("userId") Long userId, @Param("fragment") String fragment);
    
    /**
     * 获取特定视图
     */
    Optional<NavigationView> findByUserIdAndFragment(Long userId, String fragment);
    
    /**
     * 更新固定状态
     */
    @Modifying
    @Query("UPDATE NavigationView nv SET nv.isPinned = :isPinned WHERE nv.user.id = :userId AND nv.fragment = :fragment")
    void updatePinnedStatus(@Param("userId") Long userId, @Param("fragment") String fragment, @Param("isPinned") Boolean isPinned);
    
    /**
     * 删除用户的所有导航视图
     */
    void deleteByUserId(Long userId);
    
    /**
     * 获取用户的固定视图数量
     */
    @Query("SELECT COUNT(nv) FROM NavigationView nv WHERE nv.user.id = :userId AND nv.isPinned = true")
    long countPinnedViews(@Param("userId") Long userId);
}
package com.chatpass.repository;

import com.chatpass.entity.CustomEmoji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 自定义表情仓库
 */
@Repository
public interface CustomEmojiRepository extends JpaRepository<CustomEmoji, Long> {
    
    /**
     * 根据组织ID查找所有表情
     */
    List<CustomEmoji> findByRealmIdAndDeactivatedFalseOrderByUsageCountDesc(Long realmId);
    
    /**
     * 根据名称查找表情
     */
    Optional<CustomEmoji> findByRealmIdAndName(Long realmId, String name);
    
    /**
     * 根据名称查找活跃表情
     */
    Optional<CustomEmoji> findByRealmIdAndNameAndDeactivatedFalse(Long realmId, String name);
    
    /**
     * 检查名称是否已存在
     */
    boolean existsByRealmIdAndName(Long realmId, String name);
    
    /**
     * 根据创建者查找表情
     */
    List<CustomEmoji> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    /**
     * 增加使用次数
     */
    @Modifying
    @Query("UPDATE CustomEmoji e SET e.usageCount = e.usageCount + 1 WHERE e.id = :emojiId")
    void incrementUsageCount(@Param("emojiId") Long emojiId);
    
    /**
     * 搜索表情（按名称或别名）
     */
    @Query("SELECT e FROM CustomEmoji e WHERE e.realmId = :realmId AND e.deactivated = false AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.aliases) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<CustomEmoji> searchByKeyword(@Param("realmId") Long realmId, @Param("keyword") String keyword);
}

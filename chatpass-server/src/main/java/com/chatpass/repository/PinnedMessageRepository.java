package com.chatpass.repository;

import com.chatpass.entity.PinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 固定消息仓库
 */
@Repository
public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, Long> {
    
    /**
     * 根据组织ID查找所有固定消息
     */
    List<PinnedMessage> findByRealmIdAndIsExpiredFalseOrderBySortOrderAsc(Long realmId);
    
    /**
     * 根据Stream ID查找固定消息
     */
    List<PinnedMessage> findByStreamIdAndIsExpiredFalseOrderBySortOrderAsc(Long streamId);
    
    /**
     * 根据Stream和Topic查找固定消息
     */
    List<PinnedMessage> findByStreamIdAndTopicAndIsExpiredFalseOrderBySortOrderAsc(Long streamId, String topic);
    
    /**
     * 根据消息ID查找固定记录
     */
    Optional<PinnedMessage> findByMessageId(Long messageId);
    
    /**
     * 检查消息是否已固定
     */
    boolean existsByMessageId(Long messageId);
    
    /**
     * 根据固定者查找
     */
    List<PinnedMessage> findByPinnedByOrderByPinnedAtDesc(Long pinnedBy);
    
    /**
     * 更新排序顺序
     */
    @Modifying
    @Query("UPDATE PinnedMessage pm SET pm.sortOrder = :sortOrder WHERE pm.id = :id")
    void updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);
    
    /**
     * 清理过期固定消息
     */
    @Modifying
    @Query("UPDATE PinnedMessage pm SET pm.isExpired = true WHERE pm.expiresAt < :now")
    void markExpiredMessages(@Param("now") LocalDateTime now);
    
    /**
     * 统计固定消息数量
     */
    @Query("SELECT COUNT(pm) FROM PinnedMessage pm WHERE pm.streamId = :streamId AND pm.isExpired = false")
    long countByStreamIdAndNotExpired(@Param("streamId") Long streamId);
}

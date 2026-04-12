package com.chatpass.repository;

import com.chatpass.entity.MessageFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 消息标记仓库
 */
@Repository
public interface MessageFlagRepository extends JpaRepository<MessageFlag, Long> {
    
    /**
     * 根据用户ID查找标记
     */
    List<MessageFlag> findByUserIdOrderByFlaggedAtDesc(Long userId);
    
    /**
     * 根据消息ID查找标记
     */
    List<MessageFlag> findByMessageIdOrderByFlaggedAtAsc(Long messageId);
    
    /**
     * 根据用户和消息查找标记
     */
    Optional<MessageFlag> findByUserIdAndMessageId(Long userId, Long messageId);
    
    /**
     * 根据用户和标记类型查找
     */
    List<MessageFlag> findByUserIdAndFlagTypeOrderByFlaggedAtDesc(Long userId, String flagType);
    
    /**
     * 检查是否已标记
     */
    boolean existsByUserIdAndMessageId(Long userId, Long messageId);
    
    /**
     * 检查特定类型标记
     */
    boolean existsByUserIdAndMessageIdAndFlagType(Long userId, Long messageId, String flagType);
    
    /**
     * 统计用户标记数
     */
    long countByUserId(Long userId);
    
    /**
     * 统计消息标记数
     */
    @Query("SELECT COUNT(mf) FROM MessageFlag mf WHERE mf.messageId = :messageId")
    long countByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 获取标记用户列表
     */
    @Query("SELECT mf.userId FROM MessageFlag mf WHERE mf.messageId = :messageId")
    List<Long> findFlagUsers(@Param("messageId") Long messageId);
    
    /**
     * 删除标记
     */
    @Modifying
    @Query("DELETE FROM MessageFlag mf WHERE mf.userId = :userId AND mf.messageId = :messageId")
    void deleteByUserIdAndMessageId(@Param("userId") Long userId, @Param("messageId") Long messageId);
}

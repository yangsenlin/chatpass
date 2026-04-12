package com.chatpass.repository;

import com.chatpass.entity.UserReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户反应仓库
 */
@Repository
public interface UserReactionRepository extends JpaRepository<UserReaction, Long> {
    
    /**
     * 根据消息ID查找所有反应
     */
    List<UserReaction> findByMessageIdOrderByReactedAtAsc(Long messageId);
    
    /**
     * 根据消息ID和用户ID查找反应
     */
    Optional<UserReaction> findByMessageIdAndUserId(Long messageId, Long userId);
    
    /**
     * 根据消息ID和反应类型查找
     */
    List<UserReaction> findByMessageIdAndReactionType(Long messageId, String reactionType);
    
    /**
     * 根据用户ID查找反应
     */
    List<UserReaction> findByUserIdOrderByReactedAtDesc(Long userId);
    
    /**
     * 检查反应是否存在
     */
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);
    
    /**
     * 检查特定反应是否存在
     */
    boolean existsByMessageIdAndUserIdAndReactionType(Long messageId, Long userId, String reactionType);
    
    /**
     * 删除反应
     */
    @Modifying
    @Query("DELETE FROM UserReaction ur WHERE ur.messageId = :messageId AND ur.userId = :userId")
    void deleteByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    /**
     * 删除特定类型的反应
     */
    @Modifying
    @Query("DELETE FROM UserReaction ur WHERE ur.messageId = :messageId AND ur.userId = :userId AND ur.reactionType = :reactionType")
    void deleteByMessageIdUserIdAndReactionType(@Param("messageId") Long messageId, @Param("userId") Long userId, @Param("reactionType") String reactionType);
    
    /**
     * 统计消息的反应数
     */
    @Query("SELECT COUNT(ur) FROM UserReaction ur WHERE ur.messageId = :messageId")
    long countByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 统计特定类型的反应数
     */
    @Query("SELECT COUNT(ur) FROM UserReaction ur WHERE ur.messageId = :messageId AND ur.reactionType = :reactionType")
    long countByMessageIdAndReactionType(@Param("messageId") Long messageId, @Param("reactionType") String reactionType);
    
    /**
     * 获取消息的反应统计
     */
    @Query("SELECT ur.reactionType, COUNT(ur) FROM UserReaction ur WHERE ur.messageId = :messageId GROUP BY ur.reactionType")
    List<Object[]> getReactionStats(@Param("messageId") Long messageId);
    
    /**
     * 获取反应的用户列表
     */
    @Query("SELECT ur.userId FROM UserReaction ur WHERE ur.messageId = :messageId AND ur.reactionType = :reactionType")
    List<Long> findUsersByReaction(@Param("messageId") Long messageId, @Param("reactionType") String reactionType);
}

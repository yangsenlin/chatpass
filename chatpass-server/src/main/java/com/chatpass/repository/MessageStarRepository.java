package com.chatpass.repository;

import com.chatpass.entity.MessageStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MessageStarRepository
 */
@Repository
public interface MessageStarRepository extends JpaRepository<MessageStar, Long> {

    /**
     * 查找用户的所有收藏消息
     */
    @Query("SELECT s FROM MessageStar s WHERE s.user.id = :userId ORDER BY s.starredTime DESC")
    List<MessageStar> findByUserId(@Param("userId") Long userId);

    /**
     * 查找用户收藏的消息 ID 列表
     */
    @Query("SELECT s.message.id FROM MessageStar s WHERE s.user.id = :userId ORDER BY s.starredTime DESC")
    List<Long> findStarredMessageIds(@Param("userId") Long userId);

    /**
     * 查找特定消息是否被用户收藏
     */
    @Query("SELECT s FROM MessageStar s WHERE s.message.id = :messageId AND s.user.id = :userId")
    Optional<MessageStar> findByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 检查消息是否被收藏
     */
    @Query("SELECT COUNT(s) > 0 FROM MessageStar s WHERE s.message.id = :messageId AND s.user.id = :userId")
    boolean isStarred(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 统计用户收藏数量
     */
    @Query("SELECT COUNT(s) FROM MessageStar s WHERE s.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计消息被收藏次数
     */
    @Query("SELECT COUNT(s) FROM MessageStar s WHERE s.message.id = :messageId")
    Long countByMessageId(@Param("messageId") Long messageId);

    /**
     * 分页查询用户收藏
     */
    @Query("SELECT s FROM MessageStar s WHERE s.user.id = :userId ORDER BY s.starredTime DESC")
    List<MessageStar> findByUserIdPaged(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    /**
     * 删除收藏
     */
    @Query("DELETE FROM MessageStar s WHERE s.message.id = :messageId AND s.user.id = :userId")
    void deleteByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 搜索收藏（按备注）
     */
    @Query("SELECT s FROM MessageStar s WHERE s.user.id = :userId AND LOWER(s.note) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.starredTime DESC")
    List<MessageStar> searchByNote(@Param("userId") Long userId, @Param("query") String query);

    /**
     * 查找最近收藏
     */
    @Query("SELECT s FROM MessageStar s WHERE s.user.id = :userId AND s.starredTime >= :since ORDER BY s.starredTime DESC")
    List<MessageStar> findRecentStars(@Param("userId") Long userId, @Param("since") java.time.LocalDateTime since);
}
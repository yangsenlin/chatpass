package com.chatpass.repository;

import com.chatpass.entity.TypingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TypingStatusRepository
 */
@Repository
public interface TypingStatusRepository extends JpaRepository<TypingStatus, Long> {

    /**
     * 查找用户对某接收者的输入状态
     */
    @Query("SELECT t FROM TypingStatus t WHERE t.user.id = :userId AND t.recipient.id = :recipientId")
    Optional<TypingStatus> findByUserIdAndRecipientId(@Param("userId") Long userId, @Param("recipientId") Long recipientId);

    /**
     * 查找用户在某频道的输入状态
     */
    @Query("SELECT t FROM TypingStatus t WHERE t.user.id = :userId AND t.streamId = :streamId AND t.topic = :topic")
    Optional<TypingStatus> findByUserIdAndStreamAndTopic(
            @Param("userId") Long userId, 
            @Param("streamId") Long streamId, 
            @Param("topic") String topic);

    /**
     * 查找接收者正在输入的用户列表
     */
    @Query("SELECT t FROM TypingStatus t WHERE t.recipient.id = :recipientId AND t.lastUpdate >= :since ORDER BY t.lastUpdate DESC")
    List<TypingStatus> findTypingUsersForRecipient(
            @Param("recipientId") Long recipientId, 
            @Param("since") LocalDateTime since);

    /**
     * 查找频道正在输入的用户列表
     */
    @Query("SELECT t FROM TypingStatus t WHERE t.streamId = :streamId AND t.topic = :topic AND t.lastUpdate >= :since ORDER BY t.lastUpdate DESC")
    List<TypingStatus> findTypingUsersForStream(
            @Param("streamId") Long streamId, 
            @Param("topic") String topic, 
            @Param("since") LocalDateTime since);

    /**
     * 清理过期的输入状态
     */
    @Modifying
    @Query("DELETE FROM TypingStatus t WHERE t.lastUpdate < :expirationTime")
    int deleteExpiredStatus(@Param("expirationTime") LocalDateTime expirationTime);

    /**
     * 删除用户的输入状态
     */
    @Modifying
    @Query("DELETE FROM TypingStatus t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 统计正在输入的用户数量
     */
    @Query("SELECT COUNT(t) FROM TypingStatus t WHERE t.recipient.id = :recipientId AND t.lastUpdate >= :since")
    Long countTypingUsersForRecipient(@Param("recipientId") Long recipientId, @Param("since") LocalDateTime since);

    /**
     * 查找所有活跃的输入状态
     */
    @Query("SELECT t FROM TypingStatus t WHERE t.lastUpdate >= :since ORDER BY t.lastUpdate DESC")
    List<TypingStatus> findActiveTypingStatus(@Param("since") LocalDateTime since);

    /**
     * 更新输入时间
     */
    @Modifying
    @Query("UPDATE TypingStatus t SET t.lastUpdate = :newTime WHERE t.id = :id")
    void updateLastUpdate(@Param("id") Long id, @Param("newTime") LocalDateTime newTime);
}
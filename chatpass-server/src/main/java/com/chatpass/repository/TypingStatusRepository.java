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
 * 输入状态仓库
 */
@Repository
public interface TypingStatusRepository extends JpaRepository<TypingStatus, Long> {
    
    /**
     * 根据用户ID查找输入状态
     */
    List<TypingStatus> findByUserIdOrderByStartedAtDesc(Long userId);
    
    /**
     * 根据Stream和话题查找正在输入的用户
     */
    @Query("SELECT ts FROM TypingStatus ts WHERE ts.streamId = :streamId AND ts.topic = :topic")
    List<TypingStatus> findByStreamIdAndTopic(@Param("streamId") Long streamId, @Param("topic") String topic);
    
    /**
     * 根据Stream查找正在输入的用户
     */
    List<TypingStatus> findByStreamIdOrderByStartedAtDesc(Long streamId);
    
    /**
     * 清除用户的输入状态
     */
    @Modifying
    @Query("DELETE FROM TypingStatus ts WHERE ts.userId = :userId")
    void clearUserTyping(@Param("userId") Long userId);
    
    /**
     * 清除过期的输入状态
     */
    @Modifying
    @Query("DELETE FROM TypingStatus ts WHERE ts.startedAt < :threshold")
    void clearExpiredTyping(@Param("threshold") LocalDateTime threshold);
    
    /**
     * 统计正在输入的用户数
     */
    @Query("SELECT COUNT(ts) FROM TypingStatus ts WHERE ts.streamId = :streamId AND ts.topic = :topic")
    long countTypingUsers(@Param("streamId") Long streamId, @Param("topic") String topic);
}

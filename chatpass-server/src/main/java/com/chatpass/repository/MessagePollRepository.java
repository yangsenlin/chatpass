package com.chatpass.repository;

import com.chatpass.entity.MessagePoll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MessagePollRepository
 */
@Repository
public interface MessagePollRepository extends JpaRepository<MessagePoll, Long> {

    /**
     * 查找消息的投票
     */
    @Query("SELECT p FROM MessagePoll p WHERE p.messageId = :messageId")
    Optional<MessagePoll> findByMessageId(@Param("messageId") Long messageId);

    /**
     * 查找用户的投票
     */
    @Query("SELECT p FROM MessagePoll p WHERE p.creatorId = :creatorId ORDER BY p.dateCreated DESC")
    List<MessagePoll> findByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 查找活跃投票
     */
    @Query("SELECT p FROM MessagePoll p WHERE p.status = 'OPEN' AND (p.endTime IS NULL OR p.endTime > CURRENT_TIMESTAMP) ORDER BY p.dateCreated DESC")
    List<MessagePoll> findActivePolls();

    /**
     * 查找已结束投票
     */
    @Query("SELECT p FROM MessagePoll p WHERE p.status IN ('CLOSED', 'ENDED') ORDER BY p.endTime DESC")
    List<MessagePoll> findEndedPolls();

    /**
     * 统计投票数
     */
    @Query("SELECT COUNT(p) FROM MessagePoll p WHERE p.creatorId = :creatorId")
    Long countByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 查找即将结束的投票
     */
    @Query("SELECT p FROM MessagePoll p WHERE p.status = 'OPEN' AND p.endTime IS NOT NULL AND p.endTime BETWEEN :start AND :end")
    List<MessagePoll> findEndingSoon(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
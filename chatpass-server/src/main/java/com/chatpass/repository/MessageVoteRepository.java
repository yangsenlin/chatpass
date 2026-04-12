package com.chatpass.repository;

import com.chatpass.entity.MessageVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 消息投票仓库
 */
@Repository
public interface MessageVoteRepository extends JpaRepository<MessageVote, Long> {
    
    /**
     * 根据消息ID查找所有投票
     */
    List<MessageVote> findByMessageId(Long messageId);
    
    /**
     * 根据消息ID和用户ID查找投票
     */
    Optional<MessageVote> findByMessageIdAndUserId(Long messageId, Long userId);
    
    /**
     * 检查用户是否已投票
     */
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);
    
    /**
     * 统计消息的投票数
     */
    @Query("SELECT COUNT(v) FROM MessageVote v WHERE v.messageId = :messageId AND v.voteType = :voteType")
    long countByMessageIdAndVoteType(@Param("messageId") Long messageId, @Param("voteType") String voteType);
    
    /**
     * 统计消息的总投票数（上票减下票）
     */
    @Query("SELECT SUM(CASE WHEN v.voteType = 'upvote' THEN 1 ELSE -1 END) FROM MessageVote v WHERE v.messageId = :messageId")
    Long getTotalVotes(@Param("messageId") Long messageId);
    
    /**
     * 根据用户查找投票
     */
    List<MessageVote> findByUserIdOrderByVotedAtDesc(Long userId);
    
    /**
     * 删除投票
     */
    @Modifying
    @Query("DELETE FROM MessageVote v WHERE v.messageId = :messageId AND v.userId = :userId")
    void deleteByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    /**
     * 获取消息投票详情（投票用户列表）
     */
    @Query("SELECT v.userId FROM MessageVote v WHERE v.messageId = :messageId AND v.voteType = :voteType")
    List<Long> findVotersByMessageIdAndVoteType(@Param("messageId") Long messageId, @Param("voteType") String voteType);
}

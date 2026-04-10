package com.chatpass.repository;

import com.chatpass.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PollVoteRepository
 */
@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    /**
     * 查找投票的所有投票记录
     */
    @Query("SELECT v FROM PollVote v WHERE v.poll.id = :pollId AND v.isCancelled = false ORDER BY v.voteTime DESC")
    List<PollVote> findByPollId(@Param("pollId") Long pollId);

    /**
     * 查找用户的投票
     */
    @Query("SELECT v FROM PollVote v WHERE v.poll.id = :pollId AND v.userId = :userId AND v.isCancelled = false")
    List<PollVote> findByPollIdAndUserId(@Param("pollId") Long pollId, @Param("userId") Long userId);

    /**
     * 查找选项的投票数
     */
    @Query("SELECT COUNT(v) FROM PollVote v WHERE v.poll.id = :pollId AND v.optionIndex = :optionIndex AND v.isCancelled = false")
    Long countByPollIdAndOptionIndex(@Param("pollId") Long pollId, @Param("optionIndex") Integer optionIndex);

    /**
     * 统计投票总数
     */
    @Query("SELECT COUNT(v) FROM PollVote v WHERE v.poll.id = :pollId AND v.isCancelled = false")
    Long countByPollId(@Param("pollId") Long pollId);

    /**
     * 统计投票人数（多选投票的投票者）
     */
    @Query("SELECT COUNT(DISTINCT v.userId) FROM PollVote v WHERE v.poll.id = :pollId AND v.isCancelled = false")
    Long countVotersByPollId(@Param("pollId") Long pollId);

    /**
     * 查找投票者的投票记录
     */
    @Query("SELECT v FROM PollVote v WHERE v.userId = :userId ORDER BY v.voteTime DESC")
    List<PollVote> findByUserId(@Param("userId") Long userId);

    /**
     * 获取投票统计（每个选项的投票数）
     */
    @Query("SELECT v.optionIndex, COUNT(v) FROM PollVote v WHERE v.poll.id = :pollId AND v.isCancelled = false GROUP BY v.optionIndex ORDER BY v.optionIndex")
    List<Object[]> getVoteStats(@Param("pollId") Long pollId);

    /**
     * 查找用户的单选投票
     */
    @Query("SELECT v FROM PollVote v WHERE v.poll.id = :pollId AND v.userId = :userId AND v.isCancelled = false LIMIT 1")
    Optional<PollVote> findSingleVote(@Param("pollId") Long pollId, @Param("userId") Long userId);
}
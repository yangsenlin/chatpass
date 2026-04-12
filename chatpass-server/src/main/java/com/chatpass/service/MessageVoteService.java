package com.chatpass.service;

import com.chatpass.dto.MessageVoteDTO;
import com.chatpass.entity.MessageVote;
import com.chatpass.repository.MessageVoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息投票服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageVoteService {
    
    private final MessageVoteRepository voteRepository;
    
    /**
     * 投票（支持或反对）
     */
    @Transactional
    public MessageVoteDTO.VoteInfo vote(Long messageId, Long userId, String voteType, Long realmId) {
        
        // 检查是否已投票
        Optional<MessageVote> existingVote = voteRepository.findByMessageIdAndUserId(messageId, userId);
        
        if (existingVote.isPresent()) {
            // 更新投票类型
            MessageVote vote = existingVote.get();
            vote.setVoteType(voteType);
            vote = voteRepository.save(vote);
            log.info("更新投票: messageId={}, userId={}, type={}", messageId, userId, voteType);
            return toVoteInfo(vote);
        } else {
            // 新投票
            MessageVote vote = MessageVote.builder()
                    .messageId(messageId)
                    .userId(userId)
                    .voteType(voteType)
                    .realmId(realmId)
                    .build();
            
            vote = voteRepository.save(vote);
            log.info("新投票: messageId={}, userId={}, type={}", messageId, userId, voteType);
            return toVoteInfo(vote);
        }
    }
    
    /**
     * 取消投票
     */
    @Transactional
    public void unvote(Long messageId, Long userId) {
        voteRepository.deleteByMessageIdAndUserId(messageId, userId);
        log.info("取消投票: messageId={}, userId={}", messageId, userId);
    }
    
    /**
     * 获取投票统计
     */
    public MessageVoteDTO.VoteStats getVoteStats(Long messageId) {
        long upvoteCount = voteRepository.countByMessageIdAndVoteType(messageId, "upvote");
        long downvoteCount = voteRepository.countByMessageIdAndVoteType(messageId, "downvote");
        Long totalVotes = voteRepository.getTotalVotes(messageId);
        
        List<Long> upvoters = voteRepository.findVotersByMessageIdAndVoteType(messageId, "upvote");
        List<Long> downvoters = voteRepository.findVotersByMessageIdAndVoteType(messageId, "downvote");
        
        return MessageVoteDTO.VoteStats.builder()
                .messageId(messageId)
                .upvoteCount(upvoteCount)
                .downvoteCount(downvoteCount)
                .totalVotes(totalVotes != null ? totalVotes : 0L)
                .upvoters(upvoters)
                .downvoters(downvoters)
                .build();
    }
    
    /**
     * 获取用户的投票记录
     */
    public List<MessageVoteDTO.VoteInfo> getUserVotes(Long userId) {
        return voteRepository.findByUserIdOrderByVotedAtDesc(userId)
                .stream()
                .map(this::toVoteInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取消息的所有投票
     */
    public List<MessageVoteDTO.VoteInfo> getMessageVotes(Long messageId) {
        return voteRepository.findByMessageId(messageId)
                .stream()
                .map(this::toVoteInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否已投票
     */
    public boolean hasVoted(Long messageId, Long userId) {
        return voteRepository.existsByMessageIdAndUserId(messageId, userId);
    }
    
    /**
     * 获取用户的投票类型
     */
    public Optional<String> getUserVoteType(Long messageId, Long userId) {
        return voteRepository.findByMessageIdAndUserId(messageId, userId)
                .map(MessageVote::getVoteType);
    }
    
    private MessageVoteDTO.VoteInfo toVoteInfo(MessageVote vote) {
        return MessageVoteDTO.VoteInfo.builder()
                .id(vote.getId())
                .messageId(vote.getMessageId())
                .userId(vote.getUserId())
                .voteType(vote.getVoteType())
                .votedAt(vote.getVotedAt())
                .realmId(vote.getRealmId())
                .build();
    }
}

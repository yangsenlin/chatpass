package com.chatpass.service;

import com.chatpass.dto.PollDTO;
import com.chatpass.entity.MessagePoll;
import com.chatpass.entity.PollVote;
import com.chatpass.repository.MessagePollRepository;
import com.chatpass.repository.PollVoteRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PollService
 * 
 * 消息投票管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PollService {

    private final MessagePollRepository pollRepository;
    private final PollVoteRepository voteRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    /**
     * 创建投票
     */
    @Transactional
    public MessagePoll createPoll(Long messageId, Long creatorId, String question, 
                                   List<String> options, String pollType, 
                                   Boolean isAnonymous, LocalDateTime endTime) {
        try {
            String optionsJson = objectMapper.writeValueAsString(options);
            
            MessagePoll poll = MessagePoll.builder()
                    .messageId(messageId)
                    .creatorId(creatorId)
                    .question(question)
                    .options(optionsJson)
                    .pollType(pollType != null ? pollType : MessagePoll.TYPE_SINGLE)
                    .isAnonymous(isAnonymous != null ? isAnonymous : false)
                    .allowChange(true)
                    .endTime(endTime)
                    .status(MessagePoll.STATUS_OPEN)
                    .totalVotes(0L)
                    .build();

            poll = pollRepository.save(poll);

            auditLogService.logCreate(creatorId, "POLL", poll.getId(), poll);

            log.info("Poll created: {} by user {}", question, creatorId);

            return poll;
        } catch (Exception e) {
            log.error("Failed to create poll: {}", e.getMessage());
            throw new RuntimeException("创建投票失败", e);
        }
    }

    /**
     * 投票
     */
    @Transactional
    public PollVote vote(Long pollId, Long userId, Integer optionIndex) {
        MessagePoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("投票不存在"));

        // 检查投票状态
        if (poll.isEnded()) {
            throw new IllegalStateException("投票已结束");
        }

        // 检查选项范围
        List<String> options = getOptions(poll);
        if (optionIndex < 0 || optionIndex >= options.size()) {
            throw new IllegalArgumentException("无效的选项索引");
        }

        // 单选投票检查
        if (poll.getPollType().equals(MessagePoll.TYPE_SINGLE)) {
            Optional<PollVote> existingVote = voteRepository.findSingleVote(pollId, userId);
            if (existingVote.isPresent() && !poll.getAllowChange()) {
                throw new IllegalStateException("已投票且不允许修改");
            }
            // 取消旧投票
            if (existingVote.isPresent()) {
                existingVote.get().setIsCancelled(true);
                voteRepository.save(existingVote.get());
            }
        }

        // 创建投票
        PollVote vote = PollVote.builder()
                .poll(poll)
                .userId(userId)
                .optionIndex(optionIndex)
                .optionText(options.get(optionIndex))
                .isCancelled(false)
                .build();

        vote = voteRepository.save(vote);

        // 更新投票统计
        poll.setTotalVotes(voteRepository.countByPollId(pollId));
        pollRepository.save(poll);

        log.info("User {} voted on poll {}: option {}", userId, pollId, optionIndex);

        return vote;
    }

    /**
     * 多选投票
     */
    @Transactional
    public List<PollVote> multiVote(Long pollId, Long userId, List<Integer> optionIndices) {
        MessagePoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("投票不存在"));

        if (!poll.getPollType().equals(MessagePoll.TYPE_MULTIPLE)) {
            throw new IllegalArgumentException("这不是多选投票");
        }

        if (poll.isEnded()) {
            throw new IllegalStateException("投票已结束");
        }

        // 取消旧投票
        List<PollVote> oldVotes = voteRepository.findByPollIdAndUserId(pollId, userId);
        for (PollVote oldVote : oldVotes) {
            oldVote.setIsCancelled(true);
            voteRepository.save(oldVote);
        }

        // 创建新投票
        List<String> options = getOptions(poll);
        List<PollVote> votes = new ArrayList<>();

        for (Integer index : optionIndices) {
            if (index < 0 || index >= options.size()) {
                throw new IllegalArgumentException("无效的选项索引: " + index);
            }

            PollVote vote = PollVote.builder()
                    .poll(poll)
                    .userId(userId)
                    .optionIndex(index)
                    .optionText(options.get(index))
                    .isCancelled(false)
                    .build();

            votes.add(voteRepository.save(vote));
        }

        // 更新投票统计
        poll.setTotalVotes(voteRepository.countByPollId(pollId));
        pollRepository.save(poll);

        log.info("User {} multi-voted on poll {}: {} options", userId, pollId, optionIndices.size());

        return votes;
    }

    /**
     * 取消投票
     */
    @Transactional
    public void cancelVote(Long pollId, Long userId) {
        MessagePoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("投票不存在"));

        if (!poll.getAllowChange()) {
            throw new IllegalStateException("不允许修改投票");
        }

        List<PollVote> votes = voteRepository.findByPollIdAndUserId(pollId, userId);
        for (PollVote vote : votes) {
            vote.setIsCancelled(true);
            voteRepository.save(vote);
        }

        // 更新投票统计
        poll.setTotalVotes(voteRepository.countByPollId(pollId));
        pollRepository.save(poll);

        log.info("User {} cancelled vote on poll {}", userId, pollId);
    }

    /**
     * 结束投票
     */
    @Transactional
    public void endPoll(Long pollId, Long userId) {
        MessagePoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("投票不存在"));

        if (!poll.getCreatorId().equals(userId)) {
            throw new IllegalStateException("只有创建者可以结束投票");
        }

        poll.endPoll();
        pollRepository.save(poll);

        auditLogService.logUpdate(userId, "POLL", pollId, poll, poll);

        log.info("Poll {} ended by user {}", pollId, userId);
    }

    /**
     * 获取投票详情
     */
    public MessagePoll getPoll(Long pollId) {
        return pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("投票不存在"));
    }

    /**
     * 获取投票统计
     */
    public PollDTO.PollStats getPollStats(Long pollId) {
        MessagePoll poll = getPoll(pollId);
        
        List<String> options = getOptions(poll);
        List<Object[]> voteStats = voteRepository.getVoteStats(pollId);
        
        Map<Integer, Long> statsMap = new HashMap<>();
        for (Object[] stat : voteStats) {
            Integer index = (Integer) stat[0];
            Long count = (Long) stat[1];
            statsMap.put(index, count);
        }

        List<PollDTO.OptionStats> optionStats = new ArrayList<>();
        long totalVotes = voteRepository.countByPollId(pollId);

        for (int i = 0; i < options.size(); i++) {
            long votes = statsMap.getOrDefault(i, 0L);
            double percentage = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0;
            
            optionStats.add(PollDTO.OptionStats.builder()
                    .index(i)
                    .text(options.get(i))
                    .votes(votes)
                    .percentage(percentage)
                    .build());
        }

        Long voters = voteRepository.countVotersByPollId(pollId);

        return PollDTO.PollStats.builder()
                .pollId(pollId)
                .question(poll.getQuestion())
                .totalVotes(totalVotes)
                .totalVoters(voters)
                .optionStats(optionStats)
                .status(poll.getStatus())
                .build();
    }

    /**
     * 获取用户的投票
     */
    public List<Integer> getUserVotes(Long pollId, Long userId) {
        List<PollVote> votes = voteRepository.findByPollIdAndUserId(pollId, userId);
        return votes.stream()
                .map(PollVote::getOptionIndex)
                .collect(Collectors.toList());
    }

    /**
     * 获取投票选项
     */
    private List<String> getOptions(MessagePoll poll) {
        try {
            return objectMapper.readValue(poll.getOptions(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse options: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取活跃投票
     */
    public List<MessagePoll> getActivePolls() {
        return pollRepository.findActivePolls();
    }

    /**
     * 获取已结束投票
     */
    public List<MessagePoll> getEndedPolls() {
        return pollRepository.findEndedPolls();
    }

    /**
     * 转换为 DTO
     */
    public PollDTO.PollResponse toResponse(MessagePoll poll) {
        List<String> options = getOptions(poll);
        
        return PollDTO.PollResponse.builder()
                .id(poll.getId())
                .messageId(poll.getMessageId())
                .creatorId(poll.getCreatorId())
                .question(poll.getQuestion())
                .options(options)
                .pollType(poll.getPollType())
                .isAnonymous(poll.getIsAnonymous())
                .allowChange(poll.getAllowChange())
                .endTime(poll.getEndTime() != null ? poll.getEndTime().toString() : null)
                .status(poll.getStatus())
                .totalVotes(poll.getTotalVotes())
                .dateCreated(poll.getDateCreated().toString())
                .build();
    }

    /**
     * 定时检查并结束过期投票
     */
    @Transactional
    public void checkExpiredPolls() {
        List<MessagePoll> activePolls = pollRepository.findActivePolls();
        
        for (MessagePoll poll : activePolls) {
            if (poll.isEnded()) {
                poll.endPoll();
                pollRepository.save(poll);
                log.info("Poll {} auto-ended due to expiry", poll.getId());
            }
        }
    }
}
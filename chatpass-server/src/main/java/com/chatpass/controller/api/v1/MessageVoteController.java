package com.chatpass.controller.api.v1;

import com.chatpass.dto.MessageVoteDTO;
import com.chatpass.service.MessageVoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 消息投票控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MessageVoteController {
    
    private final MessageVoteService voteService;
    
    /**
     * 投票（支持）
     */
    @PostMapping("/messages/{messageId}/upvote")
    public ResponseEntity<MessageVoteDTO.VoteInfo> upvote(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long realmId) {
        
        MessageVoteDTO.VoteInfo vote = voteService.vote(messageId, userId, "upvote", realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(vote);
    }
    
    /**
     * 投票（反对）
     */
    @PostMapping("/messages/{messageId}/downvote")
    public ResponseEntity<MessageVoteDTO.VoteInfo> downvote(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long realmId) {
        
        MessageVoteDTO.VoteInfo vote = voteService.vote(messageId, userId, "downvote", realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(vote);
    }
    
    /**
     * 取消投票
     */
    @DeleteMapping("/messages/{messageId}/vote")
    public ResponseEntity<Void> unvote(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        voteService.unvote(messageId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取投票统计
     */
    @GetMapping("/messages/{messageId}/votes/stats")
    public ResponseEntity<MessageVoteDTO.VoteStats> getVoteStats(@PathVariable Long messageId) {
        MessageVoteDTO.VoteStats stats = voteService.getVoteStats(messageId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取消息的所有投票
     */
    @GetMapping("/messages/{messageId}/votes")
    public ResponseEntity<List<MessageVoteDTO.VoteInfo>> getMessageVotes(@PathVariable Long messageId) {
        List<MessageVoteDTO.VoteInfo> votes = voteService.getMessageVotes(messageId);
        return ResponseEntity.ok(votes);
    }
    
    /**
     * 获取用户的投票记录
     */
    @GetMapping("/users/{userId}/votes")
    public ResponseEntity<List<MessageVoteDTO.VoteInfo>> getUserVotes(@PathVariable Long userId) {
        List<MessageVoteDTO.VoteInfo> votes = voteService.getUserVotes(userId);
        return ResponseEntity.ok(votes);
    }
    
    /**
     * 检查用户是否已投票
     */
    @GetMapping("/messages/{messageId}/voted")
    public ResponseEntity<Boolean> hasVoted(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        boolean voted = voteService.hasVoted(messageId, userId);
        return ResponseEntity.ok(voted);
    }
    
    /**
     * 获取用户的投票类型
     */
    @GetMapping("/messages/{messageId}/vote_type")
    public ResponseEntity<String> getUserVoteType(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        Optional<String> voteType = voteService.getUserVoteType(messageId, userId);
        return voteType.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

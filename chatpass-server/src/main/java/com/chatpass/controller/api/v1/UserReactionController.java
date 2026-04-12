package com.chatpass.controller.api.v1;

import com.chatpass.dto.UserReactionDTO;
import com.chatpass.service.UserReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户反应控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserReactionController {
    
    private final UserReactionService reactionService;
    
    /**
     * 添加反应
     */
    @PostMapping("/messages/{messageId}/reactions")
    public ResponseEntity<UserReactionDTO.ReactionInfo> addReaction(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam String reactionType,
            @RequestParam(required = false) Long realmId) {
        
        UserReactionDTO.ReactionInfo reaction = reactionService.addReaction(messageId, userId, reactionType, realmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reaction);
    }
    
    /**
     * 移除反应
     */
    @DeleteMapping("/messages/{messageId}/reactions")
    public ResponseEntity<Void> removeReaction(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam(required = false) String reactionType) {
        
        reactionService.removeReaction(messageId, userId, reactionType);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取消息的所有反应
     */
    @GetMapping("/messages/{messageId}/reactions")
    public ResponseEntity<List<UserReactionDTO.ReactionInfo>> getMessageReactions(@PathVariable Long messageId) {
        List<UserReactionDTO.ReactionInfo> reactions = reactionService.getMessageReactions(messageId);
        return ResponseEntity.ok(reactions);
    }
    
    /**
     * 获取消息的反应统计
     */
    @GetMapping("/messages/{messageId}/reactions/stats")
    public ResponseEntity<UserReactionDTO.ReactionStats> getReactionStats(@PathVariable Long messageId) {
        UserReactionDTO.ReactionStats stats = reactionService.getReactionStats(messageId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取用户的反应
     */
    @GetMapping("/messages/{messageId}/reactions/user/{userId}")
    public ResponseEntity<UserReactionDTO.ReactionInfo> getUserReaction(
            @PathVariable Long messageId,
            @PathVariable Long userId) {
        
        return reactionService.getUserReaction(messageId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取用户的所有反应
     */
    @GetMapping("/users/{userId}/reactions")
    public ResponseEntity<List<UserReactionDTO.ReactionInfo>> getUserReactions(@PathVariable Long userId) {
        List<UserReactionDTO.ReactionInfo> reactions = reactionService.getUserReactions(userId);
        return ResponseEntity.ok(reactions);
    }
    
    /**
     * 检查用户是否已反应
     */
    @GetMapping("/messages/{messageId}/reactions/check")
    public ResponseEntity<Boolean> hasReaction(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        boolean hasReaction = reactionService.hasReaction(messageId, userId);
        return ResponseEntity.ok(hasReaction);
    }
    
    /**
     * 检查特定反应是否存在
     */
    @GetMapping("/messages/{messageId}/reactions/check/{reactionType}")
    public ResponseEntity<Boolean> hasSpecificReaction(
            @PathVariable Long messageId,
            @PathVariable String reactionType,
            @RequestParam Long userId) {
        
        boolean hasReaction = reactionService.hasSpecificReaction(messageId, userId, reactionType);
        return ResponseEntity.ok(hasReaction);
    }
    
    /**
     * 统计反应数量
     */
    @GetMapping("/messages/{messageId}/reactions/count")
    public ResponseEntity<Long> countReactions(@PathVariable Long messageId) {
        long count = reactionService.countReactions(messageId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 统计特定反应数量
     */
    @GetMapping("/messages/{messageId}/reactions/count/{reactionType}")
    public ResponseEntity<Long> countReactionsByType(
            @PathVariable Long messageId,
            @PathVariable String reactionType) {
        
        long count = reactionService.countReactionsByType(messageId, reactionType);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 获取特定反应的用户
     */
    @GetMapping("/messages/{messageId}/reactions/{reactionType}/users")
    public ResponseEntity<List<Long>> getReactionUsers(
            @PathVariable Long messageId,
            @PathVariable String reactionType) {
        
        List<Long> users = reactionService.getReactionUsers(messageId, reactionType);
        return ResponseEntity.ok(users);
    }
}

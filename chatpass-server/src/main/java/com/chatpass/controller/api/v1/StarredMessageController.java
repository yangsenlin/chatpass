package com.chatpass.controller.api.v1;

import com.chatpass.service.StarredMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * StarredMessageController - 消息星标API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StarredMessageController {

    private final StarredMessageService starredMessageService;

    /**
     * 星标消息
     */
    @PostMapping("/messages/{message_id}/star")
    public ResponseEntity<Map<String, Object>> starMessage(
            @PathVariable("message_id") Long messageId,
            @RequestParam Long user_id) {

        starredMessageService.starMessage(user_id, messageId);

        return ResponseEntity.ok(Map.of(
                "message_id", messageId,
                "result", "success"
        ));
    }

    /**
     * 取消星标
     */
    @DeleteMapping("/messages/{message_id}/star")
    public ResponseEntity<Map<String, Object>> unstarMessage(
            @PathVariable("message_id") Long messageId,
            @RequestParam Long user_id) {

        starredMessageService.unstarMessage(user_id, messageId);

        return ResponseEntity.ok(Map.of(
                "message_id", messageId,
                "result", "success"
        ));
    }

    /**
     * 获取用户的星标消息列表
     */
    @GetMapping("/users/{user_id}/starred")
    public ResponseEntity<Map<String, Object>> getStarredMessages(
            @PathVariable("user_id") Long userId) {

        List<Long> starredIds = starredMessageService.getStarredMessages(userId);

        return ResponseEntity.ok(Map.of(
                "user_id", userId,
                "starred_messages", starredIds,
                "count", starredIds.size()
        ));
    }

    /**
     * 获取星标统计
     */
    @GetMapping("/users/{user_id}/starred/count")
    public ResponseEntity<Map<String, Object>> getStarredCount(
            @PathVariable("user_id") Long userId) {

        Integer count = starredMessageService.getStarredCount(userId);

        return ResponseEntity.ok(Map.of(
                "user_id", userId,
                "starred_count", count
        ));
    }

    /**
     * 检查消息是否已星标
     */
    @GetMapping("/messages/{message_id}/star/check")
    public ResponseEntity<Map<String, Object>> checkStarred(
            @PathVariable("message_id") Long messageId,
            @RequestParam Long user_id) {

        boolean starred = starredMessageService.isStarred(user_id, messageId);

        return ResponseEntity.ok(Map.of(
                "message_id", messageId,
                "user_id", user_id,
                "is_starred", starred
        ));
    }
}
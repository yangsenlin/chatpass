package com.chatpass.controller.api.v1;

import com.chatpass.entity.Message;
import com.chatpass.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SearchController - 搜索增强API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 全文搜索
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMessages(
            @RequestParam Long realm_id,
            @RequestParam String query) {

        List<Message> results = searchService.searchMessages(realm_id, query);

        return ResponseEntity.ok(Map.of(
                "query", query,
                "realm_id", realm_id,
                "total_results", results.size(),
                "messages", results.stream()
                        .map(m -> Map.of(
                                "id", m.getId(),
                                "content", m.getContent() != null ? m.getContent() : "",
                                "subject", m.getSubject() != null ? m.getSubject() : "",
                                "sender_id", m.getSender().getId(),
                                "timestamp", m.getDateSent().toString()
                        ))
                        .collect(Collectors.toList())
        ));
    }

    /**
     * 在频道内搜索
     */
    @GetMapping("/search/stream/{stream_id}")
    public ResponseEntity<Map<String, Object>> searchInStream(
            @PathVariable("stream_id") Long streamId,
            @RequestParam String query) {

        List<Message> results = searchService.searchInStream(streamId, query);

        return ResponseEntity.ok(Map.of(
                "query", query,
                "stream_id", streamId,
                "total_results", results.size(),
                "messages", results.stream()
                        .map(m -> Map.of(
                                "id", m.getId(),
                                "content", m.getContent() != null ? m.getContent() : "",
                                "subject", m.getSubject() != null ? m.getSubject() : "",
                                "timestamp", m.getDateSent().toString()
                        ))
                        .collect(Collectors.toList())
        ));
    }

    /**
     * 搜索用户消息
     */
    @GetMapping("/search/user/{user_id}")
    public ResponseEntity<Map<String, Object>> searchByUser(
            @PathVariable("user_id") Long userId,
            @RequestParam String query) {

        List<Message> results = searchService.searchByUser(userId, query);

        return ResponseEntity.ok(Map.of(
                "query", query,
                "user_id", userId,
                "total_results", results.size(),
                "messages", results.stream()
                        .map(m -> Map.of(
                                "id", m.getId(),
                                "content", m.getContent() != null ? m.getContent() : "",
                                "timestamp", m.getDateSent().toString()
                        ))
                        .collect(Collectors.toList())
        ));
    }

    /**
     * 搜索话题
     */
    @GetMapping("/search/topic")
    public ResponseEntity<Map<String, Object>> searchByTopic(
            @RequestParam Long stream_id,
            @RequestParam String topic) {

        List<Message> results = searchService.searchByTopic(stream_id, topic);

        return ResponseEntity.ok(Map.of(
                "topic", topic,
                "stream_id", stream_id,
                "total_results", results.size(),
                "messages", results.stream()
                        .map(m -> Map.of(
                                "id", m.getId(),
                                "content", m.getContent() != null ? m.getContent() : "",
                                "subject", m.getSubject() != null ? m.getSubject() : "",
                                "timestamp", m.getDateSent().toString()
                        ))
                        .collect(Collectors.toList())
        ));
    }

    /**
     * 搜索统计
     */
    @GetMapping("/search/stats")
    public ResponseEntity<Map<String, Object>> getSearchStats(
            @RequestParam Long realm_id,
            @RequestParam String query) {

        Map<String, Object> stats = searchService.getSearchStats(realm_id, query);

        return ResponseEntity.ok(stats);
    }
}
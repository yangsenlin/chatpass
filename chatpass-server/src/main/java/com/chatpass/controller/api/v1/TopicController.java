package com.chatpass.controller.api.v1;

import com.chatpass.dto.MessageDTO;
import com.chatpass.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 话题管理控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TopicController {
    
    private final TopicService topicService;
    
    /**
     * 获取Stream的所有话题
     */
    @GetMapping("/streams/{streamId}/topics")
    public ResponseEntity<List<String>> getStreamTopics(@PathVariable Long streamId) {
        List<String> topics = topicService.getStreamTopics(streamId);
        return ResponseEntity.ok(topics);
    }
    
    /**
     * 获取话题统计
     */
    @GetMapping("/streams/{streamId}/topics/stats")
    public ResponseEntity<Map<String, Long>> getTopicStats(@PathVariable Long streamId) {
        Map<String, Long> stats = topicService.getTopicStats(streamId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 重命名话题
     */
    @PostMapping("/streams/{streamId}/topics/rename")
    public ResponseEntity<Integer> renameTopic(
            @PathVariable Long streamId,
            @RequestParam String oldTopic,
            @RequestParam String newTopic) {
        
        int count = topicService.renameTopic(streamId, oldTopic, newTopic);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 合并话题
     */
    @PostMapping("/streams/{streamId}/topics/merge")
    public ResponseEntity<Integer> mergeTopics(
            @PathVariable Long streamId,
            @RequestParam List<String> sourceTopics,
            @RequestParam String targetTopic) {
        
        int count = topicService.mergeTopics(streamId, sourceTopics, targetTopic);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 拆分话题
     */
    @PostMapping("/streams/{streamId}/topics/split")
    public ResponseEntity<Integer> splitTopic(
            @PathVariable Long streamId,
            @RequestParam String sourceTopic,
            @RequestParam String newTopic,
            @RequestParam List<Long> messageIds) {
        
        int count = topicService.splitTopic(streamId, sourceTopic, newTopic, messageIds);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 获取话题的最新消息
     */
    @GetMapping("/streams/{streamId}/topics/{topic}/latest")
    public ResponseEntity<MessageDTO.Response> getTopicLatestMessage(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        
        return topicService.getTopicLatestMessage(streamId, topic)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取话题的参与者
     */
    @GetMapping("/streams/{streamId}/topics/{topic}/participants")
    public ResponseEntity<Set<Long>> getTopicParticipants(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        
        Set<Long> participants = topicService.getTopicParticipants(streamId, topic);
        return ResponseEntity.ok(participants);
    }
    
    /**
     * 搜索话题
     */
    @GetMapping("/streams/{streamId}/topics/search")
    public ResponseEntity<List<String>> searchTopics(
            @PathVariable Long streamId,
            @RequestParam(required = false) String keyword) {
        
        List<String> topics = topicService.searchTopics(streamId, keyword);
        return ResponseEntity.ok(topics);
    }
    
    /**
     * 解析话题名称
     */
    @PostMapping("/topics/parse")
    public ResponseEntity<String> parseTopic(@RequestParam String content) {
        String topic = topicService.parseTopic(content);
        return ResponseEntity.ok(topic);
    }
}

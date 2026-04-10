package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.TopicDTO;
import com.chatpass.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Topic 控制器
 * 
 * 话题管理 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Topics", description = "话题管理 API")
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/streams/{streamId}/topics")
    @Operation(summary = "获取频道的话题列表")
    public ResponseEntity<ApiResponse<List<String>>> getStreamTopics(@PathVariable Long streamId) {
        List<String> topics = topicService.getStreamTopics(streamId);
        
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    @GetMapping("/streams/{streamId}/topics/stats")
    @Operation(summary = "获取频道的话题统计")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStreamTopicStats(@PathVariable Long streamId) {
        Map<String, Long> stats = topicService.getStreamTopicStats(streamId);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/streams/{streamId}/topics/hot")
    @Operation(summary = "获取热门话题")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHotTopics(
            @PathVariable Long streamId,
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> hotTopics = topicService.getHotTopics(streamId, limit);
        
        return ResponseEntity.ok(ApiResponse.success(hotTopics));
    }

    @PostMapping("/streams/{streamId}/topics/rename")
    @Operation(summary = "重命名话题")
    public ResponseEntity<ApiResponse<TopicDTO.RenameResponse>> renameTopic(
            @PathVariable Long streamId,
            @RequestBody TopicDTO.RenameRequest request) {
        int count = topicService.renameTopic(streamId, request.getOldTopic(), request.getNewTopic());
        
        return ResponseEntity.ok(ApiResponse.success(TopicDTO.RenameResponse.builder()
                .streamId(streamId)
                .oldTopic(request.getOldTopic())
                .newTopic(request.getNewTopic())
                .messagesUpdated(count)
                .build()));
    }

    @PostMapping("/streams/{streamId}/topics/merge")
    @Operation(summary = "合并话题")
    public ResponseEntity<ApiResponse<TopicDTO.MergeResponse>> mergeTopic(
            @PathVariable Long streamId,
            @RequestBody TopicDTO.MergeRequest request) {
        int count = topicService.mergeTopic(streamId, request.getSourceTopic(), request.getTargetTopic());
        
        return ResponseEntity.ok(ApiResponse.success(TopicDTO.MergeResponse.builder()
                .streamId(streamId)
                .sourceTopic(request.getSourceTopic())
                .targetTopic(request.getTargetTopic())
                .messagesMoved(count)
                .build()));
    }

    @GetMapping("/streams/{streamId}/topics/search")
    @Operation(summary = "搜索话题")
    public ResponseEntity<ApiResponse<List<String>>> searchTopics(
            @PathVariable Long streamId,
            @RequestParam String query) {
        List<String> topics = topicService.searchTopics(streamId, query);
        
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    @GetMapping("/streams/{streamId}/topics/{topic}/info")
    @Operation(summary = "获取话题详情")
    public ResponseEntity<ApiResponse<TopicDTO.InfoResponse>> getTopicInfo(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        Long messageCount = topicService.getTopicMessageCount(streamId, topic);
        String lastMessageTime = topicService.getTopicLastMessageTime(streamId, topic) != null
                ? topicService.getTopicLastMessageTime(streamId, topic).toString()
                : "never";
        
        return ResponseEntity.ok(ApiResponse.success(TopicDTO.InfoResponse.builder()
                .streamId(streamId)
                .topic(topic)
                .messageCount(messageCount)
                .lastMessageTime(lastMessageTime)
                .build()));
    }
}
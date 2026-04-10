package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.MutedTopicDTO;
import com.chatpass.entity.MutedTopic;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.MutedTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MutedTopic 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Muted Topics", description = "静音话题 API")
public class MutedTopicController {

    private final MutedTopicService mutedTopicService;
    private final SecurityUtil securityUtil;

    @PostMapping("/users/me/muted_topics")
    @Operation(summary = "静音话题")
    public ResponseEntity<ApiResponse<Void>> muteTopic(@RequestBody MutedTopicDTO.MuteRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        mutedTopicService.muteTopic(userId, request.getStreamId(), request.getTopic());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/users/me/muted_topics")
    @Operation(summary = "取消静音")
    public ResponseEntity<ApiResponse<Void>> unmuteTopic(@RequestParam Long streamId, @RequestParam String topic) {
        Long userId = securityUtil.getCurrentUserId();
        mutedTopicService.unmuteTopic(userId, streamId, topic);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me/muted_topics")
    @Operation(summary = "获取静音话题列表")
    public ResponseEntity<ApiResponse<MutedTopicDTO.ListResponse>> getMutedTopics() {
        Long userId = securityUtil.getCurrentUserId();
        List<MutedTopic> topics = mutedTopicService.getUserMutedTopics(userId);
        
        List<MutedTopicDTO.Response> responses = topics.stream()
                .map(t -> MutedTopicDTO.Response.builder()
                        .id(t.getId())
                        .streamId(t.getStreamId())
                        .topic(t.getTopicName())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(MutedTopicDTO.ListResponse.builder()
                .mutedTopics(responses)
                .count(responses.size())
                .build()));
    }
}
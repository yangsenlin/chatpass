package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.PollDTO;
import com.chatpass.entity.MessagePoll;
import com.chatpass.entity.PollVote;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.PollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Poll 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Polls", description = "消息投票 API")
public class PollController {

    private final PollService pollService;
    private final SecurityUtil securityUtil;

    @PostMapping("/polls")
    @Operation(summary = "创建投票")
    public ResponseEntity<ApiResponse<PollDTO.PollResponse>> createPoll(
            @RequestBody PollDTO.CreatePollRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        LocalDateTime endTime = request.getEndTime() != null ? 
                LocalDateTime.parse(request.getEndTime()) : null;
        
        MessagePoll poll = pollService.createPoll(
                request.getMessageId(), userId, request.getQuestion(),
                request.getOptions(), request.getPollType(),
                request.getIsAnonymous(), endTime);
        
        return ResponseEntity.ok(ApiResponse.success(pollService.toResponse(poll)));
    }

    @GetMapping("/polls/{pollId}")
    @Operation(summary = "获取投票详情")
    public ResponseEntity<ApiResponse<PollDTO.PollResponse>> getPoll(
            @PathVariable Long pollId) {
        MessagePoll poll = pollService.getPoll(pollId);
        
        return ResponseEntity.ok(ApiResponse.success(pollService.toResponse(poll)));
    }

    @GetMapping("/polls/{pollId}/stats")
    @Operation(summary = "获取投票统计")
    public ResponseEntity<ApiResponse<PollDTO.PollStats>> getPollStats(
            @PathVariable Long pollId) {
        PollDTO.PollStats stats = pollService.getPollStats(pollId);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/polls/vote")
    @Operation(summary = "投票")
    public ResponseEntity<ApiResponse<Void>> vote(@RequestBody PollDTO.VoteRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        pollService.vote(request.getPollId(), userId, request.getOptionIndex());
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/polls/multi-vote")
    @Operation(summary = "多选投票")
    public ResponseEntity<ApiResponse<Void>> multiVote(@RequestBody PollDTO.MultiVoteRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        pollService.multiVote(request.getPollId(), userId, request.getOptionIndices());
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/polls/{pollId}/cancel")
    @Operation(summary = "取消投票")
    public ResponseEntity<ApiResponse<Void>> cancelVote(@PathVariable Long pollId) {
        Long userId = securityUtil.getCurrentUserId();
        
        pollService.cancelVote(pollId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/polls/{pollId}/end")
    @Operation(summary = "结束投票")
    public ResponseEntity<ApiResponse<Void>> endPoll(@PathVariable Long pollId) {
        Long userId = securityUtil.getCurrentUserId();
        
        pollService.endPoll(pollId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/polls/{pollId}/my-votes")
    @Operation(summary = "获取我的投票")
    public ResponseEntity<ApiResponse<PollDTO.UserVoteResponse>> getMyVotes(
            @PathVariable Long pollId) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<Integer> votes = pollService.getUserVotes(pollId, userId);
        
        PollDTO.UserVoteResponse response = PollDTO.UserVoteResponse.builder()
                .pollId(pollId)
                .userId(userId)
                .optionIndices(votes)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/polls/active")
    @Operation(summary = "获取活跃投票")
    public ResponseEntity<ApiResponse<List<PollDTO.PollResponse>>> getActivePolls() {
        List<MessagePoll> polls = pollService.getActivePolls();
        
        List<PollDTO.PollResponse> response = polls.stream()
                .map(pollService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/polls/ended")
    @Operation(summary = "获取已结束投票")
    public ResponseEntity<ApiResponse<List<PollDTO.PollResponse>>> getEndedPolls() {
        List<MessagePoll> polls = pollService.getEndedPolls();
        
        List<PollDTO.PollResponse> response = polls.stream()
                .map(pollService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
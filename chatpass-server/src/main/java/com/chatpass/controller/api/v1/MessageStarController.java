package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.MessageStarDTO;
import com.chatpass.entity.MessageStar;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.MessageStarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MessageStar 控制器
 * 
 * 消息收藏 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Message Stars", description = "消息收藏 API")
public class MessageStarController {

    private final MessageStarService starService;
    private final SecurityUtil securityUtil;

    @PostMapping("/messages/{messageId}/star")
    @Operation(summary = "收藏消息")
    public ResponseEntity<ApiResponse<MessageStarDTO.StarResponse>> starMessage(
            @PathVariable Long messageId,
            @RequestBody(required = false) MessageStarDTO.StarRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        String note = request != null ? request.getNote() : null;
        MessageStar star = starService.starMessage(messageId, userId, note);
        
        return ResponseEntity.ok(ApiResponse.success(starService.toResponse(star)));
    }

    @DeleteMapping("/messages/{messageId}/star")
    @Operation(summary = "取消收藏")
    public ResponseEntity<ApiResponse<Void>> unstarMessage(@PathVariable Long messageId) {
        Long userId = securityUtil.getCurrentUserId();
        
        starService.unstarMessage(messageId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/messages/{messageId}/star")
    @Operation(summary = "检查是否收藏")
    public ResponseEntity<ApiResponse<MessageStarDTO.StarStatus>> checkStarStatus(
            @PathVariable Long messageId) {
        Long userId = securityUtil.getCurrentUserId();
        
        boolean starred = starService.isStarred(messageId, userId);
        Long starCount = starService.getMessageStarCount(messageId);
        
        MessageStarDTO.StarStatus status = MessageStarDTO.StarStatus.builder()
                .messageId(messageId)
                .starred(starred)
                .starCount(starCount)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/users/me/stars")
    @Operation(summary = "获取我的收藏列表")
    public ResponseEntity<ApiResponse<List<MessageStarDTO.StarResponse>>> getMyStars() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<MessageStar> stars = starService.getUserStars(userId);
        
        List<MessageStarDTO.StarResponse> response = stars.stream()
                .map(starService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/stars/ids")
    @Operation(summary = "获取收藏的消息 ID 列表")
    public ResponseEntity<ApiResponse<List<Long>>> getMyStarredIds() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<Long> ids = starService.getStarredMessageIds(userId);
        
        return ResponseEntity.ok(ApiResponse.success(ids));
    }

    @GetMapping("/users/me/stars/paged")
    @Operation(summary = "分页获取收藏")
    public ResponseEntity<ApiResponse<List<MessageStarDTO.StarResponse>>> getMyStarsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<MessageStar> stars = starService.getUserStarsPaged(userId, page, size);
        
        List<MessageStarDTO.StarResponse> response = stars.stream()
                .map(starService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/stars/count")
    @Operation(summary = "统计收藏数量")
    public ResponseEntity<ApiResponse<Long>> getMyStarCount() {
        Long userId = securityUtil.getCurrentUserId();
        
        Long count = starService.getStarCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/users/me/stars/summary")
    @Operation(summary = "获取收藏摘要")
    public ResponseEntity<ApiResponse<MessageStarDTO.StarSummary>> getMyStarSummary() {
        Long userId = securityUtil.getCurrentUserId();
        
        MessageStarDTO.StarSummary summary = starService.getStarSummary(userId);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PutMapping("/messages/{messageId}/star/note")
    @Operation(summary = "更新收藏备注")
    public ResponseEntity<ApiResponse<MessageStarDTO.StarResponse>> updateStarNote(
            @PathVariable Long messageId,
            @RequestBody MessageStarDTO.NoteRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        MessageStar star = starService.updateNote(messageId, userId, request.getNote());
        
        return ResponseEntity.ok(ApiResponse.success(starService.toResponse(star)));
    }

    @GetMapping("/users/me/stars/search")
    @Operation(summary = "搜索收藏")
    public ResponseEntity<ApiResponse<List<MessageStarDTO.StarResponse>>> searchStars(
            @RequestParam String query) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<MessageStar> stars = starService.searchStars(userId, query);
        
        List<MessageStarDTO.StarResponse> response = stars.stream()
                .map(starService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/stars/recent")
    @Operation(summary = "获取最近收藏")
    public ResponseEntity<ApiResponse<List<MessageStarDTO.StarResponse>>> getRecentStars(
            @RequestParam(defaultValue = "7") int days) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<MessageStar> stars = starService.getRecentStars(userId, days);
        
        List<MessageStarDTO.StarResponse> response = stars.stream()
                .map(starService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/messages/star/batch")
    @Operation(summary = "批量收藏")
    public ResponseEntity<ApiResponse<List<MessageStarDTO.StarResponse>>> starMessagesBatch(
            @RequestBody MessageStarDTO.BatchRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        List<MessageStar> stars = starService.starMessages(request.getMessageIds(), userId);
        
        List<MessageStarDTO.StarResponse> response = stars.stream()
                .map(starService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/messages/star/batch")
    @Operation(summary = "批量取消收藏")
    public ResponseEntity<ApiResponse<Void>> unstarMessagesBatch(
            @RequestBody MessageStarDTO.BatchRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        starService.unstarMessages(request.getMessageIds(), userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
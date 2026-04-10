package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.DraftDTO;
import com.chatpass.entity.Draft;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.DraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Draft 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Drafts", description = "消息草稿 API")
public class DraftController {

    private final DraftService draftService;
    private final SecurityUtil securityUtil;

    @PostMapping("/drafts")
    @Operation(summary = "保存草稿")
    public ResponseEntity<ApiResponse<DraftDTO.Response>> saveDraft(
            @RequestBody DraftDTO.SaveRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        Draft draft = draftService.saveDraft(userId, request.getRecipientId(), 
                request.getTopic(), request.getContent());
        
        return ResponseEntity.ok(ApiResponse.success(toResponse(draft)));
    }

    @GetMapping("/drafts")
    @Operation(summary = "获取用户草稿列表")
    public ResponseEntity<ApiResponse<DraftDTO.ListResponse>> getDrafts() {
        Long userId = securityUtil.getCurrentUserId();
        List<Draft> drafts = draftService.getUserDrafts(userId);
        
        List<DraftDTO.Response> responses = drafts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(DraftDTO.ListResponse.builder()
                .drafts(responses)
                .count(responses.size())
                .build()));
    }

    @GetMapping("/drafts/{recipientId}")
    @Operation(summary = "获取特定对话的草稿")
    public ResponseEntity<ApiResponse<DraftDTO.Response>> getDraft(
            @PathVariable Long recipientId,
            @RequestParam(required = false) String topic) {
        
        Long userId = securityUtil.getCurrentUserId();
        return draftService.getDraft(userId, recipientId, topic)
                .map(d -> ResponseEntity.ok(ApiResponse.success(toResponse(d))))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @DeleteMapping("/drafts/{recipientId}")
    @Operation(summary = "删除草稿")
    public ResponseEntity<ApiResponse<Void>> deleteDraft(
            @PathVariable Long recipientId,
            @RequestParam(required = false) String topic) {
        
        Long userId = securityUtil.getCurrentUserId();
        draftService.deleteDraft(userId, recipientId, topic);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/drafts")
    @Operation(summary = "清空所有草稿")
    public ResponseEntity<ApiResponse<Void>> clearAllDrafts() {
        Long userId = securityUtil.getCurrentUserId();
        draftService.clearAllDrafts(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private DraftDTO.Response toResponse(Draft draft) {
        return DraftDTO.Response.builder()
                .id(draft.getId())
                .recipientId(draft.getRecipient() != null ? draft.getRecipient().getId() : null)
                .topic(draft.getTopic())
                .content(draft.getContent())
                .lastEditTime(draft.getLastEditTime())
                .dateCreated(draft.getDateCreated())
                .build();
    }
}
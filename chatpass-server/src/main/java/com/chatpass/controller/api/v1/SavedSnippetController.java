package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.SavedSnippetDTO;
import com.chatpass.entity.SavedSnippet;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.SavedSnippetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * SavedSnippet 控制器
 * 
 * 保存片段功能 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Saved Snippets", description = "保存片段 API")
public class SavedSnippetController {

    private final SavedSnippetService savedSnippetService;
    private final SecurityUtil securityUtil;

    @PostMapping("/saved-snippets")
    @Operation(summary = "创建保存片段")
    public ResponseEntity<ApiResponse<SavedSnippetDTO.Response>> createSnippet(
            @Valid @RequestBody SavedSnippetDTO.CreateRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        SavedSnippetDTO.Response response = savedSnippetService.createSnippet(userId, realmId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/saved-snippets")
    @Operation(summary = "获取用户的保存片段列表")
    public ResponseEntity<ApiResponse<SavedSnippetDTO.ListResponse>> getSnippets() {
        Long userId = securityUtil.getCurrentUserId();
        SavedSnippetDTO.ListResponse response = savedSnippetService.getUserSnippets(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/saved-snippets/{snippetId}")
    @Operation(summary = "获取单个保存片段")
    public ResponseEntity<ApiResponse<SavedSnippetDTO.Response>> getSnippet(
            @PathVariable Long snippetId) {
        
        Long userId = securityUtil.getCurrentUserId();
        SavedSnippetDTO.Response response = savedSnippetService.getSnippet(userId, snippetId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/saved-snippets/{snippetId}")
    @Operation(summary = "更新保存片段")
    public ResponseEntity<ApiResponse<SavedSnippetDTO.Response>> updateSnippet(
            @PathVariable Long snippetId,
            @Valid @RequestBody SavedSnippetDTO.UpdateRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        SavedSnippetDTO.Response response = savedSnippetService.updateSnippet(userId, snippetId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/saved-snippets/{snippetId}")
    @Operation(summary = "删除保存片段")
    public ResponseEntity<ApiResponse<Void>> deleteSnippet(@PathVariable Long snippetId) {
        Long userId = securityUtil.getCurrentUserId();
        savedSnippetService.deleteSnippet(userId, snippetId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/saved-snippets/search")
    @Operation(summary = "搜索保存片段")
    public ResponseEntity<ApiResponse<SavedSnippetDTO.ListResponse>> searchSnippets(
            @RequestParam String keyword) {
        
        Long userId = securityUtil.getCurrentUserId();
        SavedSnippetDTO.ListResponse response = savedSnippetService.searchSnippets(userId, keyword);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/saved-snippets/count")
    @Operation(summary = "获取保存片段数量")
    public ResponseEntity<ApiResponse<Long>> getSnippetCount() {
        Long userId = securityUtil.getCurrentUserId();
        long count = savedSnippetService.getUserSnippetCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @DeleteMapping("/saved-snippets")
    @Operation(summary = "清空所有保存片段")
    public ResponseEntity<ApiResponse<Void>> clearAllSnippets() {
        Long userId = securityUtil.getCurrentUserId();
        savedSnippetService.clearAllSnippets(userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
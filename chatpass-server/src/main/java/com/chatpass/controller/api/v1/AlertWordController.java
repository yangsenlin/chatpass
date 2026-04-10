package com.chatpass.controller.api.v1;

import com.chatpass.dto.AlertWordDTO;
import com.chatpass.dto.ApiResponse;
import com.chatpass.service.AlertWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AlertWord 控制器
 * 
 * 用户自定义关键词提醒 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Alert Words", description = "关键词提醒 API")
public class AlertWordController {

    private final AlertWordService alertWordService;

    @GetMapping("/users/me/alert_words")
    @Operation(summary = "获取用户的所有 Alert Words")
    public ResponseEntity<ApiResponse<AlertWordDTO.ListResponse>> getAlertWords() {
        // TODO: 从 SecurityContext 获取用户信息
        Long userId = 1L;
        
        AlertWordDTO.ListResponse response = alertWordService.getUserAlertWords(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/users/me/alert_words")
    @Operation(summary = "添加 Alert Word")
    public ResponseEntity<ApiResponse<AlertWordDTO.Response>> addAlertWord(
            @RequestBody AlertWordDTO.CreateRequest request) {
        
        Long userId = 1L;
        Long realmId = 1L;
        
        AlertWordDTO.Response response = alertWordService.addAlertWord(userId, realmId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/users/me/alert_words/batch")
    @Operation(summary = "批量添加 Alert Words")
    public ResponseEntity<ApiResponse<AlertWordDTO.ListResponse>> addAlertWords(
            @RequestBody AlertWordDTO.BatchRequest request) {
        
        Long userId = 1L;
        Long realmId = 1L;
        
        AlertWordDTO.ListResponse response = alertWordService.addAlertWords(userId, realmId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/users/me/alert_words/{alertWordId}")
    @Operation(summary = "更新 Alert Word")
    public ResponseEntity<ApiResponse<AlertWordDTO.Response>> updateAlertWord(
            @PathVariable Long alertWordId,
            @RequestBody AlertWordDTO.UpdateRequest request) {
        
        Long userId = 1L;
        
        AlertWordDTO.Response response = alertWordService.updateAlertWord(userId, alertWordId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/users/me/alert_words/{alertWordId}")
    @Operation(summary = "删除 Alert Word")
    public ResponseEntity<ApiResponse<Void>> removeAlertWord(@PathVariable Long alertWordId) {
        Long userId = 1L;
        
        alertWordService.removeAlertWord(userId, alertWordId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/users/me/alert_words/by-word")
    @Operation(summary = "通过词语删除 Alert Word")
    public ResponseEntity<ApiResponse<Void>> removeAlertWordByText(@RequestParam String word) {
        Long userId = 1L;
        
        alertWordService.removeAlertWordByText(userId, word);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
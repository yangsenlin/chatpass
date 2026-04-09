package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.StreamDTO;
import com.chatpass.service.StreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Stream 控制器
 * 
 * Zulip Stream (频道) API
 */
@RestController
@RequestMapping("/api/v1/streams")
@RequiredArgsConstructor
@Tag(name = "Streams", description = "Stream (频道) API")
public class StreamController {

    private final StreamService streamService;

    @PostMapping
    @Operation(summary = "创建 Stream")
    public ResponseEntity<ApiResponse<StreamDTO.Response>> createStream(
            @RequestBody StreamDTO.CreateRequest request) {
        
        // TODO: 从 SecurityContext 获取用户信息
        Long realmId = 1L;
        Long creatorId = 1L;
        
        StreamDTO.Response response = streamService.create(realmId, creatorId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取 Stream 列表")
    public ResponseEntity<ApiResponse<List<StreamDTO.Response>>> listStreams(
            @RequestParam(defaultValue = "false") boolean includeDeactivated) {
        
        // TODO: 从 SecurityContext 获取 realmId
        Long realmId = 1L;
        
        List<StreamDTO.Response> streams = streamService.list(realmId, includeDeactivated);
        return ResponseEntity.ok(ApiResponse.success(streams));
    }

    @GetMapping("/{streamId}")
    @Operation(summary = "获取 Stream 详情")
    public ResponseEntity<ApiResponse<StreamDTO.Response>> getStream(
            @PathVariable Long streamId) {
        
        // TODO: 从 SecurityContext 获取 realmId
        Long realmId = 1L;
        
        StreamDTO.Response response = streamService.getById(realmId, streamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{streamId}")
    @Operation(summary = "更新 Stream")
    public ResponseEntity<ApiResponse<StreamDTO.Response>> updateStream(
            @PathVariable Long streamId,
            @RequestBody StreamDTO.UpdateRequest request) {
        
        // TODO: 从 SecurityContext 获取 realmId
        Long realmId = 1L;
        
        StreamDTO.Response response = streamService.update(realmId, streamId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{streamId}")
    @Operation(summary = "删除（停用）Stream")
    public ResponseEntity<ApiResponse<Void>> deactivateStream(@PathVariable Long streamId) {
        // TODO: 从 SecurityContext 获取 realmId
        Long realmId = 1L;
        
        streamService.deactivate(realmId, streamId);
        return ResponseEntity.ok(ApiResponse.success("Stream deactivated", null));
    }
}
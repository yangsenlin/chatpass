package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.StreamDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.StreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Stream 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Streams", description = "频道 API")
public class StreamController {

    private final StreamService streamService;
    private final SecurityUtil securityUtil;

    @GetMapping("/streams")
    @Operation(summary = "获取所有 Stream")
    public ResponseEntity<ApiResponse<List<StreamDTO.Response>>> getStreams() {
        Long realmId = securityUtil.getCurrentRealmId();
        List<StreamDTO.Response> streams = streamService.getStreams(realmId);
        return ResponseEntity.ok(ApiResponse.success(streams));
    }

    @GetMapping("/streams/{streamId}")
    @Operation(summary = "获取 Stream 详情")
    public ResponseEntity<ApiResponse<StreamDTO.Response>> getStream(@PathVariable Long streamId) {
        Long realmId = securityUtil.getCurrentRealmId();
        StreamDTO.Response stream = streamService.getById(realmId, streamId);
        return ResponseEntity.ok(ApiResponse.success(stream));
    }

    @PostMapping("/streams")
    @Operation(summary = "创建 Stream")
    public ResponseEntity<ApiResponse<StreamDTO.Response>> createStream(
            @RequestBody StreamDTO.CreateRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        StreamDTO.Response stream = streamService.create(realmId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(stream));
    }

    @PatchMapping("/streams/{streamId}")
    @Operation(summary = "更新 Stream")
    public ResponseEntity<ApiResponse<StreamDTO.Response>> updateStream(
            @PathVariable Long streamId,
            @RequestBody StreamDTO.UpdateRequest request) {
        
        Long realmId = securityUtil.getCurrentRealmId();
        StreamDTO.Response stream = streamService.update(realmId, streamId, request);
        return ResponseEntity.ok(ApiResponse.success(stream));
    }

    @DeleteMapping("/streams/{streamId}")
    @Operation(summary = "删除 Stream")
    public ResponseEntity<ApiResponse<Void>> deleteStream(@PathVariable Long streamId) {
        Long realmId = securityUtil.getCurrentRealmId();
        streamService.delete(realmId, streamId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/streams/{streamId}/subscribe")
    @Operation(summary = "订阅 Stream")
    public ResponseEntity<ApiResponse<Void>> subscribeStream(@PathVariable Long streamId) {
        Long userId = securityUtil.getCurrentUserId();
        streamService.subscribe(userId, streamId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/streams/{streamId}/subscribe")
    @Operation(summary = "取消订阅 Stream")
    public ResponseEntity<ApiResponse<Void>> unsubscribeStream(@PathVariable Long streamId) {
        Long userId = securityUtil.getCurrentUserId();
        streamService.unsubscribe(userId, streamId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/streams/{streamId}/topics")
    @Operation(summary = "获取 Stream 的所有 Topic")
    public ResponseEntity<ApiResponse<List<String>>> getTopics(@PathVariable Long streamId) {
        Long realmId = securityUtil.getCurrentRealmId();
        List<String> topics = streamService.getTopics(realmId, streamId);
        return ResponseEntity.ok(ApiResponse.success(topics));
    }
}
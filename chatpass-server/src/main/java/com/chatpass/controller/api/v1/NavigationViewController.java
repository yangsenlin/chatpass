package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.NavigationViewDTO;
import com.chatpass.entity.NavigationView;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.NavigationViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * NavigationView 控制器
 * 
 * 导航视图功能 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Navigation Views", description = "导航视图 API")
public class NavigationViewController {

    private final NavigationViewService navigationViewService;
    private final SecurityUtil securityUtil;

    @PostMapping("/users/me/navigation-views")
    @Operation(summary = "创建导航视图")
    public ResponseEntity<ApiResponse<NavigationViewDTO.Response>> createView(
            @Valid @RequestBody NavigationViewDTO.CreateRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        NavigationViewDTO.Response response = navigationViewService.createView(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/navigation-views")
    @Operation(summary = "获取用户的所有导航视图")
    public ResponseEntity<ApiResponse<NavigationViewDTO.ListResponse>> getUserViews() {
        Long userId = securityUtil.getCurrentUserId();
        NavigationViewDTO.ListResponse response = navigationViewService.getUserViews(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/navigation-views/pinned")
    @Operation(summary = "获取用户的固定视图")
    public ResponseEntity<ApiResponse<NavigationViewDTO.PinnedResponse>> getPinnedViews() {
        Long userId = securityUtil.getCurrentUserId();
        NavigationViewDTO.PinnedResponse response = navigationViewService.getPinnedViews(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/navigation-views/hidden")
    @Operation(summary = "获取用户的隐藏视图")
    public ResponseEntity<ApiResponse<NavigationViewDTO.ListResponse>> getHiddenViews() {
        Long userId = securityUtil.getCurrentUserId();
        NavigationViewDTO.ListResponse response = navigationViewService.getHiddenViews(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/me/navigation-views/{fragment}")
    @Operation(summary = "更新导航视图")
    public ResponseEntity<ApiResponse<NavigationViewDTO.Response>> updateView(
            @PathVariable String fragment,
            @Valid @RequestBody NavigationViewDTO.UpdateRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        NavigationViewDTO.Response response = navigationViewService.updateView(userId, fragment, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/users/me/navigation-views/{fragment}")
    @Operation(summary = "删除导航视图")
    public ResponseEntity<ApiResponse<Void>> deleteView(@PathVariable String fragment) {
        Long userId = securityUtil.getCurrentUserId();
        navigationViewService.deleteView(userId, fragment);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/users/me/navigation-views/{fragment}/pin")
    @Operation(summary = "固定视图")
    public ResponseEntity<ApiResponse<NavigationViewDTO.Response>> pinView(@PathVariable String fragment) {
        Long userId = securityUtil.getCurrentUserId();
        NavigationViewDTO.Response response = navigationViewService.setPinned(userId, fragment, true);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/users/me/navigation-views/{fragment}/unpin")
    @Operation(summary = "取消固定视图")
    public ResponseEntity<ApiResponse<NavigationViewDTO.Response>> unpinView(@PathVariable String fragment) {
        Long userId = securityUtil.getCurrentUserId();
        NavigationViewDTO.Response response = navigationViewService.setPinned(userId, fragment, false);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/users/me/navigation-views/batch")
    @Operation(summary = "批量固定/取消固定视图")
    public ResponseEntity<ApiResponse<Void>> batchSetPinned(
            @RequestBody NavigationViewDTO.BatchPinRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        navigationViewService.batchSetPinned(userId, request.getFragments(), request.getIsPinned());
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me/navigation-views/count")
    @Operation(summary = "获取固定视图数量")
    public ResponseEntity<ApiResponse<Long>> getPinnedViewCount() {
        Long userId = securityUtil.getCurrentUserId();
        long count = navigationViewService.getPinnedViewCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @DeleteMapping("/users/me/navigation-views")
    @Operation(summary = "清空所有导航视图")
    public ResponseEntity<ApiResponse<Void>> clearAllViews() {
        Long userId = securityUtil.getCurrentUserId();
        navigationViewService.clearAllViews(userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
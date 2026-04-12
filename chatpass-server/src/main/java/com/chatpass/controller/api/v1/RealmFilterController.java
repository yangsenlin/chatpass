package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.RealmFilterDTO;
import com.chatpass.entity.RealmFilter;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.RealmFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * RealmFilter 控制器
 * 
 * 链接转换器功能 API（组织管理员）
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Realm Filters", description = "链接转换器 API")
public class RealmFilterController {

    private final RealmFilterService realmFilterService;
    private final SecurityUtil securityUtil;

    @PostMapping("/realms/{realmId}/filters")
    @Operation(summary = "创建链接转换器")
    public ResponseEntity<ApiResponse<RealmFilterDTO.Response>> createFilter(
            @PathVariable Long realmId,
            @Valid @RequestBody RealmFilterDTO.CreateRequest request) {
        
        // 检查权限（应该属于当前组织）
        Long currentRealmId = securityUtil.getCurrentRealmId();
        if (!currentRealmId.equals(realmId) && !securityUtil.isAdmin()) {
            throw new IllegalArgumentException("Only realm admins can create filters");
        }
        
        RealmFilterDTO.Response response = realmFilterService.createFilter(realmId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/realms/{realmId}/filters")
    @Operation(summary = "获取组织的链接转换器列表")
    public ResponseEntity<ApiResponse<RealmFilterDTO.ListResponse>> getRealmFilters(
            @PathVariable Long realmId) {
        
        RealmFilterDTO.ListResponse response = realmFilterService.getRealmFilters(realmId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/filters/{filterId}")
    @Operation(summary = "获取单个链接转换器")
    public ResponseEntity<ApiResponse<RealmFilterDTO.Response>> getFilter(
            @PathVariable Long filterId) {
        
        RealmFilterDTO.Response response = realmFilterService.getFilter(filterId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/realms/{realmId}/filters/{filterId}")
    @Operation(summary = "更新链接转换器")
    public ResponseEntity<ApiResponse<RealmFilterDTO.Response>> updateFilter(
            @PathVariable Long realmId,
            @PathVariable Long filterId,
            @Valid @RequestBody RealmFilterDTO.UpdateRequest request) {
        
        // 检查权限
        Long currentRealmId = securityUtil.getCurrentRealmId();
        if (!currentRealmId.equals(realmId) && !securityUtil.isAdmin()) {
            throw new IllegalArgumentException("Only realm admins can update filters");
        }
        
        RealmFilterDTO.Response response = realmFilterService.updateFilter(realmId, filterId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/realms/{realmId}/filters/{filterId}")
    @Operation(summary = "删除链接转换器")
    public ResponseEntity<ApiResponse<Void>> deleteFilter(
            @PathVariable Long realmId,
            @PathVariable Long filterId) {
        
        // 检查权限
        Long currentRealmId = securityUtil.getCurrentRealmId();
        if (!currentRealmId.equals(realmId) && !securityUtil.isAdmin()) {
            throw new IllegalArgumentException("Only realm admins can delete filters");
        }
        
        realmFilterService.deleteFilter(realmId, filterId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/realms/{realmId}/filters/order")
    @Operation(summary = "批量更新顺序")
    public ResponseEntity<ApiResponse<Void>> updateOrderBatch(
            @PathVariable Long realmId,
            @RequestBody RealmFilterDTO.BatchOrderUpdateRequest request) {
        
        // 检查权限
        Long currentRealmId = securityUtil.getCurrentRealmId();
        if (!currentRealmId.equals(realmId) && !securityUtil.isAdmin()) {
            throw new IllegalArgumentException("Only realm admins can reorder filters");
        }
        
        realmFilterService.updateOrderBatch(realmId, request.getUpdates());
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.entity.CustomProfileField;
import com.chatpass.entity.CustomProfileFieldValue;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.CustomProfileFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CustomProfileField 控制器
 * 
 * 自定义用户字段 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Custom Profile Fields", description = "自定义用户字段 API")
public class CustomProfileFieldController {

    private final CustomProfileFieldService fieldService;
    private final SecurityUtil securityUtil;

    @GetMapping("/realm/custom_profile_fields")
    @Operation(summary = "获取 Realm 的所有自定义字段")
    public ResponseEntity<ApiResponse<List<CustomProfileField>>> getRealmFields() {
        Long realmId = securityUtil.getCurrentRealmId();
        List<CustomProfileField> fields = fieldService.getRealmFields(realmId);
        return ResponseEntity.ok(ApiResponse.success(fields));
    }

    @PostMapping("/realm/custom_profile_fields")
    @Operation(summary = "创建自定义字段")
    public ResponseEntity<ApiResponse<CustomProfileField>> createField(
            @RequestParam String name,
            @RequestParam Integer fieldType,
            @RequestParam(required = false) String hint,
            @RequestParam(required = false) Boolean required,
            @RequestParam(required = false) Boolean displayInProfileSummary,
            @RequestParam(required = false) Boolean editableByUser,
            @RequestParam(required = false) String fieldData) {
        
        Long realmId = securityUtil.getCurrentRealmId();
        CustomProfileField field = fieldService.createField(
                realmId, name, hint, fieldType, required, 
                displayInProfileSummary, editableByUser, fieldData);
        
        return ResponseEntity.ok(ApiResponse.success(field));
    }

    @PatchMapping("/realm/custom_profile_fields/{id}")
    @Operation(summary = "更新自定义字段")
    public ResponseEntity<ApiResponse<CustomProfileField>> updateField(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String hint,
            @RequestParam(required = false) Boolean required,
            @RequestParam(required = false) Boolean displayInProfileSummary,
            @RequestParam(required = false) Boolean editableByUser,
            @RequestParam(required = false) String fieldData) {
        
        CustomProfileField field = fieldService.updateField(
                id, name, hint, required, displayInProfileSummary, editableByUser, fieldData);
        
        return ResponseEntity.ok(ApiResponse.success(field));
    }

    @DeleteMapping("/realm/custom_profile_fields/{id}")
    @Operation(summary = "删除自定义字段")
    public ResponseEntity<ApiResponse<Void>> deleteField(@PathVariable Long id) {
        fieldService.deleteField(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/realm/custom_profile_fields/reorder")
    @Operation(summary = "重排序字段")
    public ResponseEntity<ApiResponse<Void>> reorderFields(@RequestBody List<Long> fieldIds) {
        Long realmId = securityUtil.getCurrentRealmId();
        fieldService.reorderFields(realmId, fieldIds);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me/custom_profile_field_values")
    @Operation(summary = "获取当前用户的字段值")
    public ResponseEntity<ApiResponse<List<CustomProfileFieldValue>>> getUserFieldValues() {
        Long userId = securityUtil.getCurrentUserId();
        List<CustomProfileFieldValue> values = fieldService.getUserFieldValues(userId);
        return ResponseEntity.ok(ApiResponse.success(values));
    }

    @GetMapping("/users/me/custom_profile_field_values/map")
    @Operation(summary = "获取当前用户的字段值（Map格式）")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserFieldValuesMap() {
        Long userId = securityUtil.getCurrentUserId();
        Map<String, String> values = fieldService.getUserFieldValuesMap(userId);
        return ResponseEntity.ok(ApiResponse.success(values));
    }

    @PostMapping("/users/me/custom_profile_field_values/{fieldId}")
    @Operation(summary = "设置用户字段值")
    public ResponseEntity<ApiResponse<CustomProfileFieldValue>> setFieldValue(
            @PathVariable Long fieldId,
            @RequestParam String value) {
        
        Long userId = securityUtil.getCurrentUserId();
        CustomProfileFieldValue fieldValue = fieldService.setFieldValue(userId, fieldId, value);
        return ResponseEntity.ok(ApiResponse.success(fieldValue));
    }

    @DeleteMapping("/users/me/custom_profile_field_values/{fieldId}")
    @Operation(summary = "删除用户字段值")
    public ResponseEntity<ApiResponse<Void>> deleteFieldValue(@PathVariable Long fieldId) {
        Long userId = securityUtil.getCurrentUserId();
        fieldService.deleteFieldValue(userId, fieldId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/{userId}/custom_profile_field_values")
    @Operation(summary = "获取指定用户的字段值")
    public ResponseEntity<ApiResponse<List<CustomProfileFieldValue>>> getUserFieldValuesById(
            @PathVariable Long userId) {
        List<CustomProfileFieldValue> values = fieldService.getUserFieldValues(userId);
        return ResponseEntity.ok(ApiResponse.success(values));
    }
}
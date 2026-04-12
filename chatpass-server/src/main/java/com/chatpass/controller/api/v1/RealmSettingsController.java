package com.chatpass.controller.api.v1;

import com.chatpass.dto.RealmSettingsDTO;
import com.chatpass.service.RealmSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 组织配置控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class RealmSettingsController {
    
    private final RealmSettingsService settingsService;
    
    /**
     * 设置配置项
     */
    @PostMapping("/realm/{realmId}/settings")
    public ResponseEntity<RealmSettingsDTO> setSetting(
            @PathVariable Long realmId,
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean editable,
            @RequestParam(required = false) Boolean isPublic) {
        
        RealmSettingsDTO setting = settingsService.setSetting(realmId, key, value, type, description, editable, isPublic);
        return ResponseEntity.status(HttpStatus.CREATED).body(setting);
    }
    
    /**
     * 获取配置项
     */
    @GetMapping("/realm/{realmId}/settings/{key}")
    public ResponseEntity<RealmSettingsDTO> getSetting(
            @PathVariable Long realmId,
            @PathVariable String key) {
        
        return settingsService.getSetting(realmId, key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取配置值
     */
    @GetMapping("/realm/{realmId}/settings/{key}/value")
    public ResponseEntity<String> getSettingValue(
            @PathVariable Long realmId,
            @PathVariable String key) {
        
        return settingsService.getSettingValue(realmId, key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取所有配置
     */
    @GetMapping("/realm/{realmId}/settings")
    public ResponseEntity<List<RealmSettingsDTO>> getAllSettings(@PathVariable Long realmId) {
        List<RealmSettingsDTO> settings = settingsService.getAllSettings(realmId);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 获取公开配置
     */
    @GetMapping("/realm/{realmId}/settings/public")
    public ResponseEntity<List<RealmSettingsDTO>> getPublicSettings(@PathVariable Long realmId) {
        List<RealmSettingsDTO> settings = settingsService.getPublicSettings(realmId);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 获取可编辑配置
     */
    @GetMapping("/realm/{realmId}/settings/editable")
    public ResponseEntity<List<RealmSettingsDTO>> getEditableSettings(@PathVariable Long realmId) {
        List<RealmSettingsDTO> settings = settingsService.getEditableSettings(realmId);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 获取配置项（Map格式）
     */
    @GetMapping("/realm/{realmId}/settings/map")
    public ResponseEntity<Map<String, String>> getSettingsMap(@PathVariable Long realmId) {
        Map<String, String> settings = settingsService.getSettingsMap(realmId);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 删除配置项
     */
    @DeleteMapping("/realm/{realmId}/settings/{key}")
    public ResponseEntity<Void> deleteSetting(
            @PathVariable Long realmId,
            @PathVariable String key) {
        
        settingsService.deleteSetting(realmId, key);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 批量设置配置
     */
    @PostMapping("/realm/{realmId}/settings/batch")
    public ResponseEntity<Void> setMultipleSettings(
            @PathVariable Long realmId,
            @RequestBody Map<String, String> settings) {
        
        settingsService.setMultipleSettings(realmId, settings);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 检查配置是否存在
     */
    @GetMapping("/realm/{realmId}/settings/{key}/exists")
    public ResponseEntity<Boolean> existsSetting(
            @PathVariable Long realmId,
            @PathVariable String key) {
        
        boolean exists = settingsService.existsSetting(realmId, key);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 获取布尔配置
     */
    @GetMapping("/realm/{realmId}/settings/{key}/boolean")
    public ResponseEntity<Boolean> getBooleanSetting(
            @PathVariable Long realmId,
            @PathVariable String key,
            @RequestParam(defaultValue = "false") boolean defaultValue) {
        
        boolean value = settingsService.getBooleanSetting(realmId, key, defaultValue);
        return ResponseEntity.ok(value);
    }
    
    /**
     * 获取整数配置
     */
    @GetMapping("/realm/{realmId}/settings/{key}/int")
    public ResponseEntity<Integer> getIntSetting(
            @PathVariable Long realmId,
            @PathVariable String key,
            @RequestParam(defaultValue = "0") int defaultValue) {
        
        int value = settingsService.getIntSetting(realmId, key, defaultValue);
        return ResponseEntity.ok(value);
    }
}

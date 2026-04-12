package com.chatpass.service;

import com.chatpass.dto.RealmSettingsDTO;
import com.chatpass.entity.RealmSettings;
import com.chatpass.repository.RealmSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 组织配置服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealmSettingsService {
    
    private final RealmSettingsRepository settingsRepository;
    
    /**
     * 设置配置项
     */
    @Transactional
    public RealmSettingsDTO setSetting(Long realmId, String key, String value, 
                                        String type, String description, 
                                        Boolean editable, Boolean isPublic) {
        
        RealmSettings setting = settingsRepository.findByRealmIdAndSettingKey(realmId, key)
                .orElseGet(() -> RealmSettings.builder()
                        .realmId(realmId)
                        .settingKey(key)
                        .settingType(type != null ? type : "string")
                        .editable(editable != null ? editable : true)
                        .isPublic(isPublic != null ? isPublic : true)
                        .build());
        
        // 检查是否可编辑
        if (!setting.getEditable()) {
            throw new IllegalStateException("配置项不可编辑");
        }
        
        setting.setSettingValue(value);
        
        if (description != null) {
            setting.setDescription(description);
        }
        
        setting = settingsRepository.save(setting);
        log.info("设置组织配置: realmId={}, key={}, value={}", realmId, key, value);
        
        return toDTO(setting);
    }
    
    /**
     * 获取配置项
     */
    public Optional<RealmSettingsDTO> getSetting(Long realmId, String key) {
        return settingsRepository.findByRealmIdAndSettingKey(realmId, key)
                .map(this::toDTO);
    }
    
    /**
     * 获取配置值
     */
    public Optional<String> getSettingValue(Long realmId, String key) {
        return settingsRepository.findByRealmIdAndSettingKey(realmId, key)
                .map(RealmSettings::getSettingValue);
    }
    
    /**
     * 获取所有配置
     */
    public List<RealmSettingsDTO> getAllSettings(Long realmId) {
        return settingsRepository.findByRealmId(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取公开配置
     */
    public List<RealmSettingsDTO> getPublicSettings(Long realmId) {
        return settingsRepository.findPublicSettings(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取可编辑配置
     */
    public List<RealmSettingsDTO> getEditableSettings(Long realmId) {
        return settingsRepository.findEditableSettings(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取配置项（Map格式）
     */
    public Map<String, String> getSettingsMap(Long realmId) {
        return settingsRepository.findByRealmId(realmId)
                .stream()
                .collect(Collectors.toMap(
                    RealmSettings::getSettingKey,
                    RealmSettings::getSettingValue
                ));
    }
    
    /**
     * 删除配置项
     */
    @Transactional
    public void deleteSetting(Long realmId, String key) {
        settingsRepository.deleteByRealmIdAndKey(realmId, key);
        log.info("删除组织配置: realmId={}, key={}", realmId, key);
    }
    
    /**
     * 批量设置配置
     */
    @Transactional
    public void setMultipleSettings(Long realmId, Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            setSetting(realmId, entry.getKey(), entry.getValue(), null, null, null, null);
        }
        log.info("批量设置组织配置: realmId={}, count={}", realmId, settings.size());
    }
    
    /**
     * 检查配置是否存在
     */
    public boolean existsSetting(Long realmId, String key) {
        return settingsRepository.existsByRealmIdAndSettingKey(realmId, key);
    }
    
    /**
     * 获取布尔配置
     */
    public boolean getBooleanSetting(Long realmId, String key, boolean defaultValue) {
        return getSettingValue(realmId, key)
                .map(v -> Boolean.parseBoolean(v))
                .orElse(defaultValue);
    }
    
    /**
     * 获取整数配置
     */
    public int getIntSetting(Long realmId, String key, int defaultValue) {
        return getSettingValue(realmId, key)
                .map(v -> {
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
    
    private RealmSettingsDTO toDTO(RealmSettings setting) {
        return RealmSettingsDTO.builder()
                .id(setting.getId())
                .realmId(setting.getRealmId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .settingType(setting.getSettingType())
                .description(setting.getDescription())
                .editable(setting.getEditable())
                .isPublic(setting.getIsPublic())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}

package com.chatpass.service;

import com.chatpass.entity.CustomProfileField;
import com.chatpass.entity.CustomProfileFieldValue;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.CustomProfileFieldRepository;
import com.chatpass.repository.CustomProfileFieldValueRepository;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CustomProfileFieldService - 自定义用户字段服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomProfileFieldService {

    private final CustomProfileFieldRepository fieldRepository;
    private final CustomProfileFieldValueRepository valueRepository;
    private final RealmRepository realmRepository;
    private final UserProfileRepository userRepository;

    /**
     * 获取 Realm 的所有自定义字段
     */
    @Transactional(readOnly = true)
    public List<CustomProfileField> getRealmFields(Long realmId) {
        return fieldRepository.findByRealmIdOrderByOrder(realmId);
    }

    /**
     * 创建自定义字段
     */
    @Transactional
    public CustomProfileField createField(Long realmId, String name, String hint, 
                                          Integer fieldType, Boolean required, 
                                          Boolean displayInSummary, Boolean editableByUser,
                                          String fieldData) {
        
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new RuntimeException("Realm not found"));
        
        // 检查名称是否已存在
        if (fieldRepository.existsByRealmIdAndName(realmId, name)) {
            throw new RuntimeException("Field name already exists");
        }
        
        // 获取下一个排序号
        Integer maxOrder = fieldRepository.getMaxOrder(realmId);
        Integer nextOrder = (maxOrder == null || maxOrder < 0) ? 0 : maxOrder + 1;
        
        CustomProfileField field = CustomProfileField.builder()
                .realm(realm)
                .name(name)
                .hint(hint != null ? hint : "")
                .fieldType(fieldType)
                .order(nextOrder)
                .required(required != null ? required : false)
                .displayInProfileSummary(displayInSummary != null ? displayInSummary : false)
                .editableByUser(editableByUser != null ? editableByUser : true)
                .fieldData(fieldData != null ? fieldData : "")
                .build();
        
        return fieldRepository.save(field);
    }

    /**
     * 更新自定义字段
     */
    @Transactional
    public CustomProfileField updateField(Long fieldId, String name, String hint,
                                          Boolean required, Boolean displayInSummary,
                                          Boolean editableByUser, String fieldData) {
        
        CustomProfileField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
        
        if (name != null && !name.equals(field.getName())) {
            if (fieldRepository.existsByRealmIdAndName(field.getRealm().getId(), name)) {
                throw new RuntimeException("Field name already exists");
            }
            field.setName(name);
        }
        
        if (hint != null) field.setHint(hint);
        if (required != null) field.setRequired(required);
        if (displayInSummary != null) field.setDisplayInProfileSummary(displayInSummary);
        if (editableByUser != null) field.setEditableByUser(editableByUser);
        if (fieldData != null) field.setFieldData(fieldData);
        
        return fieldRepository.save(field);
    }

    /**
     * 删除自定义字段
     */
    @Transactional
    public void deleteField(Long fieldId) {
        CustomProfileField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
        
        // 删除所有值
        List<CustomProfileFieldValue> values = valueRepository.findByFieldId(fieldId);
        valueRepository.deleteAll(values);
        
        // 删除字段
        fieldRepository.delete(field);
    }

    /**
     * 重排序字段
     */
    @Transactional
    public void reorderFields(Long realmId, List<Long> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            Long fieldId = fieldIds.get(i);
            CustomProfileField field = fieldRepository.findById(fieldId)
                    .orElseThrow(() -> new RuntimeException("Field not found"));
            field.setOrder(i);
            fieldRepository.save(field);
        }
    }

    /**
     * 获取用户的字段值
     */
    @Transactional(readOnly = true)
    public List<CustomProfileFieldValue> getUserFieldValues(Long userId) {
        return valueRepository.findByUserIdWithField(userId);
    }

    /**
     * 设置用户字段值
     */
    @Transactional
    public CustomProfileFieldValue setFieldValue(Long userId, Long fieldId, String value) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CustomProfileField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
        
        // 检查用户是否可编辑
        if (!field.getEditableByUser()) {
            throw new RuntimeException("Field is not editable by user");
        }
        
        // 查找或创建值
        CustomProfileFieldValue fieldValue = valueRepository.findByUserIdAndFieldId(userId, fieldId)
                .orElse(CustomProfileFieldValue.builder()
                        .userProfile(user)
                        .field(field)
                        .build());
        
        fieldValue.setValue(value);
        return valueRepository.save(fieldValue);
    }

    /**
     * 删除用户字段值
     */
    @Transactional
    public void deleteFieldValue(Long userId, Long fieldId) {
        valueRepository.deleteByUserIdAndFieldId(userId, fieldId);
    }

    /**
     * 获取用户的字段值（Map格式）
     */
    @Transactional(readOnly = true)
    public Map<String, String> getUserFieldValuesMap(Long userId) {
        List<CustomProfileFieldValue> values = getUserFieldValues(userId);
        return values.stream()
                .collect(Collectors.toMap(
                        v -> v.getField().getName(),
                        v -> v.getValue() != null ? v.getValue() : "",
                        (v1, v2) -> v2
                ));
    }
}
package com.chatpass.service;

import com.chatpass.dto.RealmFilterDTO;
import com.chatpass.entity.Realm;
import com.chatpass.entity.RealmFilter;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.RealmFilterRepository;
import com.chatpass.repository.RealmRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 链接转换器服务
 * 
 * 组织级别的自动链接转换规则管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealmFilterService {

    private final RealmFilterRepository realmFilterRepository;
    private final RealmRepository realmRepository;
    private final ObjectMapper objectMapper;

    private static final String CACHE_NAME = "realm_filters";

    /**
     * 创建链接转换器
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#realmId")
    public RealmFilterDTO.Response createFilter(Long realmId, RealmFilterDTO.CreateRequest request) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // 检查模式是否已存在
        if (realmFilterRepository.existsByRealmIdAndPattern(realmId, request.getPattern())) {
            throw new IllegalArgumentException("Filter pattern already exists in this realm");
        }

        // 验证正则表达式（简单验证）
        validatePattern(request.getPattern());

        // 获取下一个顺序号
        Integer nextOrder = realmFilterRepository.getNextOrder(realmId);

        RealmFilter filter = RealmFilter.builder()
                .realm(realm)
                .pattern(request.getPattern())
                .urlTemplate(request.getUrlTemplate())
                .exampleInput(request.getExampleInput())
                .reverseTemplate(request.getReverseTemplate())
                .alternativeUrlTemplates(toJson(request.getAlternativeUrlTemplates()))
                .order(nextOrder)
                .build();

        filter = realmFilterRepository.save(filter);
        log.info("Created realm filter {} for realm {}", filter.getId(), realmId);

        return toResponse(filter);
    }

    /**
     * 更新链接转换器
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#realmId")
    public RealmFilterDTO.Response updateFilter(Long realmId, Long filterId, RealmFilterDTO.UpdateRequest request) {
        RealmFilter filter = realmFilterRepository.findById(filterId)
                .orElseThrow(() -> new ResourceNotFoundException("RealmFilter", filterId));

        if (!filter.getRealm().getId().equals(realmId)) {
            throw new IllegalArgumentException("Filter does not belong to this realm");
        }

        if (request.getPattern() != null) {
            // 检查新模式是否已存在（排除自己）
            if (!filter.getPattern().equals(request.getPattern()) &&
                    realmFilterRepository.existsByRealmIdAndPattern(realmId, request.getPattern())) {
                throw new IllegalArgumentException("Filter pattern already exists in this realm");
            }
            validatePattern(request.getPattern());
            filter.setPattern(request.getPattern());
        }

        if (request.getUrlTemplate() != null) {
            filter.setUrlTemplate(request.getUrlTemplate());
        }

        if (request.getExampleInput() != null) {
            filter.setExampleInput(request.getExampleInput());
        }

        if (request.getReverseTemplate() != null) {
            filter.setReverseTemplate(request.getReverseTemplate());
        }

        if (request.getAlternativeUrlTemplates() != null) {
            filter.setAlternativeUrlTemplates(toJson(request.getAlternativeUrlTemplates()));
        }

        if (request.getOrder() != null) {
            filter.setOrder(request.getOrder());
        }

        filter = realmFilterRepository.save(filter);
        log.info("Updated realm filter {}", filterId);

        return toResponse(filter);
    }

    /**
     * 删除链接转换器
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#realmId")
    public void deleteFilter(Long realmId, Long filterId) {
        RealmFilter filter = realmFilterRepository.findById(filterId)
                .orElseThrow(() -> new ResourceNotFoundException("RealmFilter", filterId));

        if (!filter.getRealm().getId().equals(realmId)) {
            throw new IllegalArgumentException("Filter does not belong to this realm");
        }

        realmFilterRepository.delete(filter);
        log.info("Deleted realm filter {}", filterId);
    }

    /**
     * 获取组织的所有链接转换器
     */
    @Transactional(readOnly = true)
    public RealmFilterDTO.ListResponse getRealmFilters(Long realmId) {
        List<RealmFilter> filters = realmFilterRepository.findByRealmIdOrderByOrder(realmId);
        
        List<RealmFilterDTO.Response> responses = filters.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return RealmFilterDTO.ListResponse.builder()
                .filters(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取组织的链接转换器（带缓存）
     * 用于消息处理时快速获取
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "#realmId")
    public List<RealmFilter> getRealmFiltersCached(Long realmId) {
        return realmFilterRepository.findByRealmIdOrderByOrder(realmId);
    }

    /**
     * 批量更新顺序
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#realmId")
    public void updateOrderBatch(Long realmId, List<RealmFilterDTO.OrderUpdateRequest> updates) {
        for (RealmFilterDTO.OrderUpdateRequest update : updates) {
            realmFilterRepository.updateOrder(update.getId(), update.getOrder());
        }
        log.info("Updated order for {} filters in realm {}", updates.size(), realmId);
    }

    /**
     * 获取单个链接转换器
     */
    @Transactional(readOnly = true)
    public RealmFilterDTO.Response getFilter(Long filterId) {
        RealmFilter filter = realmFilterRepository.findById(filterId)
                .orElseThrow(() -> new ResourceNotFoundException("RealmFilter", filterId));
        return toResponse(filter);
    }

    /**
     * 验证正则表达式模式（基础验证）
     */
    private void validatePattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be empty");
        }
        // 简单验证：尝试编译正则表达式
        try {
            java.util.regex.Pattern.compile(pattern);
        } catch (java.util.regex.PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage());
        }
    }

    /**
     * 列表转 JSON
     */
    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize alternative URL templates", e);
            return null;
        }
    }

    /**
     * JSON 转列表
     */
    private List<String> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize alternative URL templates", e);
            return List.of();
        }
    }

    /**
     * 转换为响应
     */
    private RealmFilterDTO.Response toResponse(RealmFilter filter) {
        return RealmFilterDTO.Response.builder()
                .id(filter.getId())
                .pattern(filter.getPattern())
                .urlTemplate(filter.getUrlTemplate())
                .exampleInput(filter.getExampleInput())
                .reverseTemplate(filter.getReverseTemplate())
                .alternativeUrlTemplates(fromJson(filter.getAlternativeUrlTemplates()))
                .order(filter.getOrder())
                .dateCreated(filter.getDateCreated())
                .build();
    }
}
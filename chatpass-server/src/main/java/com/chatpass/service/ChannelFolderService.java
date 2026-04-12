package com.chatpass.service;

import com.chatpass.dto.ChannelFolderDTO;
import com.chatpass.entity.ChannelFolder;
import com.chatpass.repository.ChannelFolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 频道分类服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelFolderService {
    
    private final ChannelFolderRepository folderRepository;
    
    /**
     * 创建分类
     */
    @Transactional
    public ChannelFolderDTO createFolder(Long realmId, String name, String description, Long createdBy) {
        // 检查名称是否已存在
        if (folderRepository.existsByRealmIdAndName(realmId, name)) {
            throw new IllegalArgumentException("分类名称已存在: " + name);
        }
        
        // 获取最大排序号
        Integer maxSort = folderRepository.findMaxSortOrderByRealmId(realmId);
        int sortOrder = (maxSort != null) ? maxSort + 1 : 0;
        
        ChannelFolder folder = new ChannelFolder();
        folder.setName(name);
        folder.setDescription(description);
        folder.setRealmId(realmId);
        folder.setSortOrder(sortOrder);
        folder.setIsDefault(false);
        folder.setCreatedBy(createdBy);
        
        folder = folderRepository.save(folder);
        log.info("创建频道分类: {} (realmId: {})", name, realmId);
        
        return toDTO(folder);
    }
    
    /**
     * 获取组织的所有分类
     */
    public List<ChannelFolderDTO> getFoldersByRealm(Long realmId) {
        return folderRepository.findByRealmIdOrderBySortOrderAsc(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取分类详情
     */
    public Optional<ChannelFolderDTO> getFolderById(Long folderId) {
        return folderRepository.findById(folderId).map(this::toDTO);
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public ChannelFolderDTO updateFolder(Long folderId, String name, String description, Integer sortOrder) {
        ChannelFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + folderId));
        
        if (name != null && !name.equals(folder.getName())) {
            if (folderRepository.existsByRealmIdAndName(folder.getRealmId(), name)) {
                throw new IllegalArgumentException("分类名称已存在: " + name);
            }
            folder.setName(name);
        }
        
        if (description != null) {
            folder.setDescription(description);
        }
        
        if (sortOrder != null) {
            folder.setSortOrder(sortOrder);
        }
        
        folder = folderRepository.save(folder);
        log.info("更新频道分类: {}", folder.getName());
        
        return toDTO(folder);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteFolder(Long folderId) {
        ChannelFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + folderId));
        
        if (folder.getIsDefault()) {
            throw new IllegalStateException("不能删除默认分类");
        }
        
        folderRepository.delete(folder);
        log.info("删除频道分类: {}", folder.getName());
    }
    
    /**
     * 设置默认分类
     */
    @Transactional
    public void setDefaultFolder(Long realmId, Long folderId) {
        // 取消原默认分类
        folderRepository.findByRealmIdAndIsDefaultTrue(realmId)
                .ifPresent(f -> {
                    f.setIsDefault(false);
                    folderRepository.save(f);
                });
        
        // 设置新默认分类
        ChannelFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + folderId));
        
        folder.setIsDefault(true);
        folderRepository.save(folder);
        log.info("设置默认频道分类: {} (realmId: {})", folder.getName(), realmId);
    }
    
    /**
     * 获取或创建默认分类
     */
    @Transactional
    public ChannelFolderDTO getOrCreateDefaultFolder(Long realmId) {
        return folderRepository.findByRealmIdAndIsDefaultTrue(realmId)
                .map(this::toDTO)
                .orElseGet(() -> {
                    ChannelFolder folder = new ChannelFolder();
                    folder.setName("未分类");
                    folder.setDescription("默认频道分类");
                    folder.setRealmId(realmId);
                    folder.setSortOrder(0);
                    folder.setIsDefault(true);
                    
                    folder = folderRepository.save(folder);
                    log.info("创建默认频道分类 (realmId: {})", realmId);
                    
                    return toDTO(folder);
                });
    }
    
    private ChannelFolderDTO toDTO(ChannelFolder folder) {
        return ChannelFolderDTO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .realmId(folder.getRealmId())
                .sortOrder(folder.getSortOrder())
                .isDefault(folder.getIsDefault())
                .createdBy(folder.getCreatedBy())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}

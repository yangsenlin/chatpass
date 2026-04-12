package com.chatpass.controller.api.v1;

import com.chatpass.dto.ChannelFolderDTO;
import com.chatpass.service.ChannelFolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 频道分类控制器
 */
@RestController
@RequestMapping("/api/v1/realm/{realmId}/channel_folders")
@RequiredArgsConstructor
@Slf4j
public class ChannelFolderController {
    
    private final ChannelFolderService folderService;
    
    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<ChannelFolderDTO> createFolder(
            @PathVariable Long realmId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long createdBy) {
        
        ChannelFolderDTO folder = folderService.createFolder(realmId, name, description, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }
    
    /**
     * 获取所有分类
     */
    @GetMapping
    public ResponseEntity<List<ChannelFolderDTO>> getFolders(@PathVariable Long realmId) {
        List<ChannelFolderDTO> folders = folderService.getFoldersByRealm(realmId);
        return ResponseEntity.ok(folders);
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/{folderId}")
    public ResponseEntity<ChannelFolderDTO> getFolder(@PathVariable Long folderId) {
        return folderService.getFolderById(folderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新分类
     */
    @PatchMapping("/{folderId}")
    public ResponseEntity<ChannelFolderDTO> updateFolder(
            @PathVariable Long folderId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer sortOrder) {
        
        ChannelFolderDTO folder = folderService.updateFolder(folderId, name, description, sortOrder);
        return ResponseEntity.ok(folder);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 设置默认分类
     */
    @PostMapping("/{folderId}/default")
    public ResponseEntity<Void> setDefaultFolder(
            @PathVariable Long realmId,
            @PathVariable Long folderId) {
        
        folderService.setDefaultFolder(realmId, folderId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取默认分类
     */
    @GetMapping("/default")
    public ResponseEntity<ChannelFolderDTO> getDefaultFolder(@PathVariable Long realmId) {
        ChannelFolderDTO folder = folderService.getOrCreateDefaultFolder(realmId);
        return ResponseEntity.ok(folder);
    }
}

package com.chatpass.service;

import com.chatpass.dto.StreamPermissionDTO;
import com.chatpass.entity.StreamPermission;
import com.chatpass.repository.StreamPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stream权限服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreamPermissionService {
    
    private final StreamPermissionRepository permissionRepository;
    
    /**
     * 添加用户权限
     */
    @Transactional
    public StreamPermissionDTO addPermission(Long streamId, Long userId, String permissionType, Long realmId) {
        
        // 检查是否已存在权限
        if (permissionRepository.existsByStreamIdAndUserId(streamId, userId)) {
            throw new IllegalStateException("用户已有权限");
        }
        
        StreamPermission permission = StreamPermission.builder()
                .streamId(streamId)
                .userId(userId)
                .permissionType(permissionType)
                .realmId(realmId)
                .canRead(true)
                .canWrite("guest".equals(permissionType) ? false : true)
                .canModifyTopic("member".equals(permissionType) || "admin".equals(permissionType) || "owner".equals(permissionType))
                .canManageMembers("admin".equals(permissionType) || "owner".equals(permissionType))
                .canDeleteMessages("owner".equals(permissionType))
                .build();
        
        permission = permissionRepository.save(permission);
        log.info("添加Stream权限: streamId={}, userId={}, type={}", streamId, userId, permissionType);
        
        return toDTO(permission);
    }
    
    /**
     * 更新权限
     */
    @Transactional
    public StreamPermissionDTO updatePermission(Long streamId, Long userId, 
                                                  String permissionType,
                                                  Boolean canRead, Boolean canWrite,
                                                  Boolean canModifyTopic, Boolean canManageMembers,
                                                  Boolean canDeleteMessages) {
        
        StreamPermission permission = permissionRepository.findByStreamIdAndUserId(streamId, userId)
                .orElseThrow(() -> new IllegalArgumentException("权限不存在"));
        
        if (permissionType != null) {
            permission.setPermissionType(permissionType);
        }
        
        if (canRead != null) {
            permission.setCanRead(canRead);
        }
        
        if (canWrite != null) {
            permission.setCanWrite(canWrite);
        }
        
        if (canModifyTopic != null) {
            permission.setCanModifyTopic(canModifyTopic);
        }
        
        if (canManageMembers != null) {
            permission.setCanManageMembers(canManageMembers);
        }
        
        if (canDeleteMessages != null) {
            permission.setCanDeleteMessages(canDeleteMessages);
        }
        
        permission = permissionRepository.save(permission);
        log.info("更新Stream权限: streamId={}, userId={}", streamId, userId);
        
        return toDTO(permission);
    }
    
    /**
     * 删除权限
     */
    @Transactional
    public void removePermission(Long streamId, Long userId) {
        permissionRepository.deleteByStreamIdAndUserId(streamId, userId);
        log.info("删除Stream权限: streamId={}, userId={}", streamId, userId);
    }
    
    /**
     * 获取Stream的所有权限
     */
    public List<StreamPermissionDTO> getStreamPermissions(Long streamId) {
        return permissionRepository.findByStreamId(streamId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的权限
     */
    public List<StreamPermissionDTO> getUserPermissions(Long userId) {
        return permissionRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户对Stream的权限
     */
    public Optional<StreamPermissionDTO> getPermission(Long streamId, Long userId) {
        return permissionRepository.findByStreamIdAndUserId(streamId, userId)
                .map(this::toDTO);
    }
    
    /**
     * 检查用户是否有权限
     */
    public boolean hasPermission(Long streamId, Long userId) {
        return permissionRepository.existsByStreamIdAndUserId(streamId, userId);
    }
    
    /**
     * 检查用户是否有读权限
     */
    public boolean canRead(Long streamId, Long userId) {
        return permissionRepository.findByStreamIdAndUserId(streamId, userId)
                .map(StreamPermission::getCanRead)
                .orElse(false);
    }
    
    /**
     * 检查用户是否有写权限
     */
    public boolean canWrite(Long streamId, Long userId) {
        return permissionRepository.findByStreamIdAndUserId(streamId, userId)
                .map(StreamPermission::getCanWrite)
                .orElse(false);
    }
    
    /**
     * 获取Stream的管理员
     */
    public List<StreamPermissionDTO> getStreamAdmins(Long streamId) {
        return permissionRepository.findStreamAdmins(streamId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取可读用户列表
     */
    public List<Long> getUsersWithReadAccess(Long streamId) {
        return permissionRepository.findUsersWithReadAccess(streamId);
    }
    
    /**
     * 获取可写用户列表
     */
    public List<Long> getUsersWithWriteAccess(Long streamId) {
        return permissionRepository.findUsersWithWriteAccess(streamId);
    }
    
    /**
     * 统计成员数量
     */
    public long countMembers(Long streamId) {
        return permissionRepository.countByStreamId(streamId);
    }
    
    private StreamPermissionDTO toDTO(StreamPermission permission) {
        return StreamPermissionDTO.builder()
                .id(permission.getId())
                .streamId(permission.getStreamId())
                .userId(permission.getUserId())
                .permissionType(permission.getPermissionType())
                .canRead(permission.getCanRead())
                .canWrite(permission.getCanWrite())
                .canModifyTopic(permission.getCanModifyTopic())
                .canManageMembers(permission.getCanManageMembers())
                .canDeleteMessages(permission.getCanDeleteMessages())
                .realmId(permission.getRealmId())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}

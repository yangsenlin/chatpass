package com.chatpass.repository;

import com.chatpass.entity.StreamPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Stream权限仓库
 */
@Repository
public interface StreamPermissionRepository extends JpaRepository<StreamPermission, Long> {
    
    /**
     * 根据Stream ID查找所有权限
     */
    List<StreamPermission> findByStreamId(Long streamId);
    
    /**
     * 根据用户ID查找权限
     */
    List<StreamPermission> findByUserId(Long userId);
    
    /**
     * 根据Stream和用户查找权限
     */
    Optional<StreamPermission> findByStreamIdAndUserId(Long streamId, Long userId);
    
    /**
     * 检查用户是否有权限
     */
    boolean existsByStreamIdAndUserId(Long streamId, Long userId);
    
    /**
     * 查找Stream的管理员
     */
    @Query("SELECT sp FROM StreamPermission sp WHERE sp.streamId = :streamId AND sp.permissionType IN ('owner', 'admin')")
    List<StreamPermission> findStreamAdmins(@Param("streamId") Long streamId);
    
    /**
     * 查找Stream的所有者
     */
    Optional<StreamPermission> findByStreamIdAndPermissionType(Long streamId, String permissionType);
    
    /**
     * 删除用户权限
     */
    @Modifying
    @Query("DELETE FROM StreamPermission sp WHERE sp.streamId = :streamId AND sp.userId = :userId")
    void deleteByStreamIdAndUserId(@Param("streamId") Long streamId, @Param("userId") Long userId);
    
    /**
     * 统计Stream成员数量
     */
    long countByStreamId(Long streamId);
    
    /**
     * 查找可读权限的用户
     */
    @Query("SELECT sp.userId FROM StreamPermission sp WHERE sp.streamId = :streamId AND sp.canRead = true")
    List<Long> findUsersWithReadAccess(@Param("streamId") Long streamId);
    
    /**
     * 查找可写权限的用户
     */
    @Query("SELECT sp.userId FROM StreamPermission sp WHERE sp.streamId = :streamId AND sp.canWrite = true")
    List<Long> findUsersWithWriteAccess(@Param("streamId") Long streamId);
}

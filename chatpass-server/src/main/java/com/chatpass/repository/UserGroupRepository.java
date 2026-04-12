package com.chatpass.repository;

import com.chatpass.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户组仓库
 */
@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    
    /**
     * 根据组织ID查找所有组
     */
    List<UserGroup> findByRealmId(Long realmId);
    
    /**
     * 根据组织ID查找公开组
     */
    List<UserGroup> findByRealmIdAndIsPublicTrue(Long realmId);
    
    /**
     * 根据组织ID和名称查找组
     */
    Optional<UserGroup> findByRealmIdAndName(Long realmId, String name);
    
    /**
     * 根据创建者查找组
     */
    List<UserGroup> findByCreatedBy(Long createdBy);
    
    /**
     * 检查组名称是否已存在
     */
    boolean existsByRealmIdAndName(Long realmId, String name);
    
    /**
     * 查找用户所在的组（通过成员表）
     */
    @Query("SELECT ug FROM UserGroup ug JOIN UserGroupMember ugm ON ug.id = ugm.groupId WHERE ugm.userId = :userId")
    List<UserGroup> findGroupsByUserId(@Param("userId") Long userId);
}

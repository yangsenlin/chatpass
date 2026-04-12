package com.chatpass.repository;

import com.chatpass.entity.UserGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户组成员仓库
 */
@Repository
public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, Long> {
    
    /**
     * 根据组ID查找所有成员
     */
    List<UserGroupMember> findByGroupId(Long groupId);
    
    /**
     * 根据组ID和用户ID查找成员
     */
    Optional<UserGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 根据用户ID查找其所在的所有组
     */
    List<UserGroupMember> findByUserId(Long userId);
    
    /**
     * 查找组的所有主人
     */
    List<UserGroupMember> findByGroupIdAndIsOwnerTrue(Long groupId);
    
    /**
     * 检查用户是否在组中
     */
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 统计组成员数量
     */
    long countByGroupId(Long groupId);
    
    /**
     * 删除组成员
     */
    @Modifying
    @Query("DELETE FROM UserGroupMember ugm WHERE ugm.groupId = :groupId AND ugm.userId = :userId")
    void deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}

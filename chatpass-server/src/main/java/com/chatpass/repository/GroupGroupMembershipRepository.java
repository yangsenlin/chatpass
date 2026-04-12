package com.chatpass.repository;

import com.chatpass.entity.GroupGroupMembership;
import com.chatpass.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * GroupGroupMembership Repository
 * 
 * 用户组嵌套关系数据访问层
 */
@Repository
public interface GroupGroupMembershipRepository extends JpaRepository<GroupGroupMembership, Long> {

    /**
     * 查找父组的所有直接子组关系
     */
    List<GroupGroupMembership> findBySupergroupId(Long supergroupId);

    /**
     * 查找子组的所有直接父组关系
     */
    List<GroupGroupMembership> findBySubgroupId(Long subgroupId);

    /**
     * 获取父组的所有直接子组
     */
    @Query("SELECT ggm.subgroup FROM GroupGroupMembership ggm WHERE ggm.supergroup.id = :supergroupId")
    List<UserGroup> findSubgroupsBySupergroupId(@Param("supergroupId") Long supergroupId);

    /**
     * 获取子组的所有直接父组
     */
    @Query("SELECT ggm.supergroup FROM GroupGroupMembership ggm WHERE ggm.subgroup.id = :subgroupId")
    List<UserGroup> findSupergroupsBySubgroupId(@Param("subgroupId") Long subgroupId);

    /**
     * 检查嵌套关系是否存在
     */
    @Query("SELECT CASE WHEN COUNT(ggm) > 0 THEN true ELSE false END " +
           "FROM GroupGroupMembership ggm " +
           "WHERE ggm.supergroup.id = :supergroupId AND ggm.subgroup.id = :subgroupId")
    boolean existsBySupergroupIdAndSubgroupId(@Param("supergroupId") Long supergroupId, 
                                               @Param("subgroupId") Long subgroupId);

    /**
     * 获取特定的嵌套关系
     */
    Optional<GroupGroupMembership> findBySupergroupIdAndSubgroupId(Long supergroupId, Long subgroupId);

    /**
     * 删除嵌套关系
     */
    @Modifying
    @Query("DELETE FROM GroupGroupMembership ggm " +
           "WHERE ggm.supergroup.id = :supergroupId AND ggm.subgroup.id = :subgroupId")
    void deleteBySupergroupIdAndSubgroupId(@Param("supergroupId") Long supergroupId, 
                                           @Param("subgroupId") Long subgroupId);

    /**
     * 删除所有包含指定组的嵌套关系（作为父组或子组）
     */
    @Modifying
    @Query("DELETE FROM GroupGroupMembership ggm " +
           "WHERE ggm.supergroup.id = :groupId OR ggm.subgroup.id = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);

    /**
     * 统计父组的直接子组数量
     */
    @Query("SELECT COUNT(ggm) FROM GroupGroupMembership ggm WHERE ggm.supergroup.id = :supergroupId")
    Long countSubgroupsBySupergroupId(@Param("supergroupId") Long supergroupId);

    /**
     * 统计子组的直接父组数量
     */
    @Query("SELECT COUNT(ggm) FROM GroupGroupMembership ggm WHERE ggm.subgroup.id = :subgroupId")
    Long countSupergroupsBySubgroupId(@Param("subgroupId") Long subgroupId);

    /**
     * 递归查询：获取所有子孙组ID（包括自己）
     * 使用 CTE (Common Table Expression) 递归查询
     */
    @Query(value = """
        WITH RECURSIVE descendant_groups AS (
            SELECT id FROM user_groups WHERE id = :groupId
            UNION ALL
            SELECT ug.id 
            FROM user_groups ug
            INNER JOIN group_group_memberships ggm ON ug.id = ggm.subgroup_id
            INNER JOIN descendant_groups dg ON ggm.supergroup_id = dg.id
        )
        SELECT id FROM descendant_groups
        """, nativeQuery = true)
    List<Long> findAllDescendantGroupIds(@Param("groupId") Long groupId);

    /**
     * 递归查询：获取所有祖先组ID（包括自己）
     */
    @Query(value = """
        WITH RECURSIVE ancestor_groups AS (
            SELECT id FROM user_groups WHERE id = :groupId
            UNION ALL
            SELECT ug.id 
            FROM user_groups ug
            INNER JOIN group_group_memberships ggm ON ug.id = ggm.supergroup_id
            INNER JOIN ancestor_groups ag ON ggm.subgroup_id = ag.id
        )
        SELECT id FROM ancestor_groups
        """, nativeQuery = true)
    List<Long> findAllAncestorGroupIds(@Param("groupId") Long groupId);

    /**
     * 检查是否会造成循环引用
     * 如果 subgroup 的子孙组中包含 supergroup，则会造成循环
     */
    @Query(value = """
        WITH RECURSIVE descendant_groups AS (
            SELECT id FROM user_groups WHERE id = :subgroupId
            UNION ALL
            SELECT ug.id 
            FROM user_groups ug
            INNER JOIN group_group_memberships ggm ON ug.id = ggm.subgroup_id
            INNER JOIN descendant_groups dg ON ggm.supergroup_id = dg.id
        )
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM descendant_groups
        WHERE id = :supergroupId
        """, nativeQuery = true)
    boolean wouldCreateCycle(@Param("supergroupId") Long supergroupId, 
                             @Param("subgroupId") Long subgroupId);
}
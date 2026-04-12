package com.chatpass.repository;

import com.chatpass.entity.ChannelFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 频道分类仓库
 */
@Repository
public interface ChannelFolderRepository extends JpaRepository<ChannelFolder, Long> {
    
    /**
     * 根据组织ID查找所有分类
     */
    List<ChannelFolder> findByRealmIdOrderBySortOrderAsc(Long realmId);
    
    /**
     * 根据组织ID和名称查找分类
     */
    Optional<ChannelFolder> findByRealmIdAndName(Long realmId, String name);
    
    /**
     * 查找组织的默认分类
     */
    Optional<ChannelFolder> findByRealmIdAndIsDefaultTrue(Long realmId);
    
    /**
     * 查找组织的最大排序号
     */
    @Query("SELECT MAX(cf.sortOrder) FROM ChannelFolder cf WHERE cf.realmId = :realmId")
    Integer findMaxSortOrderByRealmId(@Param("realmId") Long realmId);
    
    /**
     * 检查分类名称是否已存在
     */
    boolean existsByRealmIdAndName(Long realmId, String name);
}

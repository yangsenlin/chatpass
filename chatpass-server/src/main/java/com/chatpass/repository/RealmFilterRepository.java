package com.chatpass.repository;

import com.chatpass.entity.RealmFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RealmFilter Repository
 */
@Repository
public interface RealmFilterRepository extends JpaRepository<RealmFilter, Long> {
    
    /**
     * 获取组织的所有链接转换器（按顺序排列）
     */
    List<RealmFilter> findByRealmIdOrderByOrder(Long realmId);
    
    /**
     * 检查模式是否存在
     */
    @Query("SELECT CASE WHEN COUNT(rf) > 0 THEN true ELSE false END FROM RealmFilter rf WHERE rf.realm.id = :realmId AND rf.pattern = :pattern")
    boolean existsByRealmIdAndPattern(@Param("realmId") Long realmId, @Param("pattern") String pattern);
    
    /**
     * 获取组织的链接转换器数量
     */
    long countByRealmId(Long realmId);
    
    /**
     * 删除组织的所有链接转换器
     */
    void deleteByRealmId(Long realmId);
    
    /**
     * 批量更新顺序
     */
    @Modifying
    @Query("UPDATE RealmFilter rf SET rf.order = :order WHERE rf.id = :id")
    void updateOrder(@Param("id") Long id, @Param("order") Integer order);
    
    /**
     * 获取组织的下一个顺序号
     */
    @Query("SELECT COALESCE(MAX(rf.order), -1) + 1 FROM RealmFilter rf WHERE rf.realm.id = :realmId")
    Integer getNextOrder(@Param("realmId") Long realmId);
}
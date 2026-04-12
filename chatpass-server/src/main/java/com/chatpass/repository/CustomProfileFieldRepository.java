package com.chatpass.repository;

import com.chatpass.entity.CustomProfileField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomProfileFieldRepository extends JpaRepository<CustomProfileField, Long> {

    /**
     * 获取 Realm 的所有自定义字段
     */
    @Query("SELECT f FROM CustomProfileField f WHERE f.realm.id = :realmId ORDER BY f.order")
    List<CustomProfileField> findByRealmIdOrderByOrder(@Param("realmId") Long realmId);

    /**
     * 获取 Realm 的在摘要中显示的字段
     */
    @Query("SELECT f FROM CustomProfileField f WHERE f.realm.id = :realmId AND f.displayInProfileSummary = true ORDER BY f.order")
    List<CustomProfileField> findByRealmIdAndDisplayInSummary(@Param("realmId") Long realmId);

    /**
     * 获取 Realm 的必填字段
     */
    @Query("SELECT f FROM CustomProfileField f WHERE f.realm.id = :realmId AND f.required = true ORDER BY f.order")
    List<CustomProfileField> findByRealmIdAndRequired(@Param("realmId") Long realmId);

    /**
     * 获取 Realm 的用于用户匹配的字段
     */
    @Query("SELECT f FROM CustomProfileField f WHERE f.realm.id = :realmId AND f.useForUserMatching = true")
    List<CustomProfileField> findByRealmIdAndUseForMatching(@Param("realmId") Long realmId);

    /**
     * 检查字段名称是否存在
     */
    @Query("SELECT COUNT(f) > 0 FROM CustomProfileField f WHERE f.realm.id = :realmId AND f.name = :name")
    boolean existsByRealmIdAndName(@Param("realmId") Long realmId, @Param("name") String name);

    /**
     * 获取 Realm 的最大排序号
     */
    @Query("SELECT COALESCE(MAX(f.order), -1) FROM CustomProfileField f WHERE f.realm.id = :realmId")
    Integer getMaxOrder(@Param("realmId") Long realmId);
}
package com.chatpass.repository;

import com.chatpass.entity.RealmSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 组织配置仓库
 */
@Repository
public interface RealmSettingsRepository extends JpaRepository<RealmSettings, Long> {
    
    /**
     * 根据组织ID查找所有配置
     */
    List<RealmSettings> findByRealmId(Long realmId);
    
    /**
     * 根据组织ID和配置键查找
     */
    Optional<RealmSettings> findByRealmIdAndSettingKey(Long realmId, String settingKey);
    
    /**
     * 查找公开配置
     */
    @Query("SELECT rs FROM RealmSettings rs WHERE rs.realmId = :realmId AND rs.isPublic = true")
    List<RealmSettings> findPublicSettings(@Param("realmId") Long realmId);
    
    /**
     * 查找可编辑配置
     */
    @Query("SELECT rs FROM RealmSettings rs WHERE rs.realmId = :realmId AND rs.editable = true")
    List<RealmSettings> findEditableSettings(@Param("realmId") Long realmId);
    
    /**
     * 检查配置是否存在
     */
    boolean existsByRealmIdAndSettingKey(Long realmId, String settingKey);
    
    /**
     * 更新配置值
     */
    @Modifying
    @Query("UPDATE RealmSettings rs SET rs.settingValue = :value WHERE rs.realmId = :realmId AND rs.settingKey = :key")
    void updateSettingValue(@Param("realmId") Long realmId, @Param("key") String key, @Param("value") String value);
    
    /**
     * 删除配置
     */
    @Modifying
    @Query("DELETE FROM RealmSettings rs WHERE rs.realmId = :realmId AND rs.settingKey = :key")
    void deleteByRealmIdAndKey(@Param("realmId") Long realmId, @Param("key") String key);
}

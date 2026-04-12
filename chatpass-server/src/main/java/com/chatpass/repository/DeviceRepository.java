package com.chatpass.repository;

import com.chatpass.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备仓库
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    /**
     * 根据用户ID查找所有设备
     */
    List<Device> findByUserIdOrderByLastActiveDesc(Long userId);
    
    /**
     * 根据设备唯一标识查找
     */
    Optional<Device> findByDeviceId(String deviceId);
    
    /**
     * 查找用户的当前设备
     */
    Optional<Device> findByUserIdAndIsCurrentTrue(Long userId);
    
    /**
     * 检查设备ID是否已存在
     */
    boolean existsByDeviceId(String deviceId);
    
    /**
     * 统计用户的设备数量
     */
    long countByUserId(Long userId);
    
    /**
     * 更新设备最后活跃时间
     */
    @Modifying
    @Query("UPDATE Device d SET d.lastActive = :lastActive WHERE d.deviceId = :deviceId")
    void updateLastActive(@Param("deviceId") String deviceId, @Param("lastActive") LocalDateTime lastActive);
    
    /**
     * 清除用户所有设备的当前标记
     */
    @Modifying
    @Query("UPDATE Device d SET d.isCurrent = false WHERE d.userId = :userId")
    void clearCurrentFlag(@Param("userId") Long userId);
    
    /**
     * 删除用户的非活跃设备
     */
    @Modifying
    @Query("DELETE FROM Device d WHERE d.userId = :userId AND d.lastActive < :threshold")
    void deleteInactiveDevices(@Param("userId") Long userId, @Param("threshold") LocalDateTime threshold);
}

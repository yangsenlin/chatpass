package com.chatpass.service;

import com.chatpass.dto.DeviceDTO;
import com.chatpass.entity.Device;
import com.chatpass.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 设备管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    
    /**
     * 注册或更新设备
     */
    @Transactional
    public DeviceDTO registerDevice(Long userId, String deviceType, String deviceName, 
                                     String os, String browser, String ipAddress) {
        
        // 生成设备唯一标识
        String deviceId = generateDeviceId(userId, deviceType, os, browser);
        
        // 查找或创建设备
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseGet(() -> {
                    Device newDevice = new Device();
                    newDevice.setUserId(userId);
                    newDevice.setDeviceId(deviceId);
                    newDevice.setDeviceType(deviceType);
                    newDevice.setDeviceName(deviceName);
                    newDevice.setOs(os);
                    newDevice.setBrowser(browser);
                    newDevice.setIsCurrent(true);
                    return newDevice;
                });
        
        // 更新设备信息
        device.setDeviceName(deviceName);
        device.setIpAddress(ipAddress);
        device.setLastActive(LocalDateTime.now());
        device.setLastLogin(LocalDateTime.now());
        
        // 清除其他设备的当前标记
        deviceRepository.clearCurrentFlag(userId);
        device.setIsCurrent(true);
        
        device = deviceRepository.save(device);
        log.info("注册设备: {} (userId: {}, type: {})", deviceName, userId, deviceType);
        
        return toDTO(device);
    }
    
    /**
     * 获取用户的所有设备
     */
    public List<DeviceDTO> getDevicesByUser(Long userId) {
        return deviceRepository.findByUserIdOrderByLastActiveDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取设备详情
     */
    public Optional<DeviceDTO> getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId).map(this::toDTO);
    }
    
    /**
     * 更新设备活跃状态
     */
    @Transactional
    public void updateDeviceActivity(String deviceId) {
        deviceRepository.updateLastActive(deviceId, LocalDateTime.now());
    }
    
    /**
     * 设置当前设备
     */
    @Transactional
    public void setCurrentDevice(Long userId, Long deviceId) {
        // 清除所有设备的当前标记
        deviceRepository.clearCurrentFlag(userId);
        
        // 设置指定设备为当前设备
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));
        
        device.setIsCurrent(true);
        deviceRepository.save(device);
        log.info("设置当前设备: {} (userId: {})", device.getDeviceName(), userId);
    }
    
    /**
     * 删除设备
     */
    @Transactional
    public void deleteDevice(Long userId, Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));
        
        if (!device.getUserId().equals(userId)) {
            throw new IllegalArgumentException("设备不属于该用户");
        }
        
        if (device.getIsCurrent()) {
            throw new IllegalStateException("不能删除当前使用的设备");
        }
        
        deviceRepository.delete(device);
        log.info("删除设备: {} (userId: {})", device.getDeviceName(), userId);
    }
    
    /**
     * 清理非活跃设备
     */
    @Transactional
    public int cleanupInactiveDevices(Long userId, int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        deviceRepository.deleteInactiveDevices(userId, threshold);
        
        long remaining = deviceRepository.countByUserId(userId);
        log.info("清理非活跃设备 (userId: {}, 保留: {})", userId, remaining);
        
        return (int) remaining;
    }
    
    /**
     * 更新推送通知设置
     */
    @Transactional
    public void updatePushNotifications(Long deviceId, boolean enabled) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));
        
        device.setPushNotificationsEnabled(enabled);
        deviceRepository.save(device);
        log.info("更新推送设置: {} (enabled: {})", device.getDeviceName(), enabled);
    }
    
    /**
     * 获取当前设备
     */
    public Optional<DeviceDTO> getCurrentDevice(Long userId) {
        return deviceRepository.findByUserIdAndIsCurrentTrue(userId)
                .map(this::toDTO);
    }
    
    /**
     * 生成设备唯一标识
     */
    private String generateDeviceId(Long userId, String deviceType, String os, String browser) {
        // 基于用户ID和设备信息生成唯一标识
        String base = String.format("%d-%s-%s-%s", userId, deviceType, os, browser);
        return UUID.nameUUIDFromBytes(base.getBytes()).toString();
    }
    
    private DeviceDTO toDTO(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .deviceType(device.getDeviceType())
                .deviceName(device.getDeviceName())
                .os(device.getOs())
                .browser(device.getBrowser())
                .ipAddress(device.getIpAddress())
                .deviceId(device.getDeviceId())
                .lastActive(device.getLastActive())
                .lastLogin(device.getLastLogin())
                .isCurrent(device.getIsCurrent())
                .pushNotificationsEnabled(device.getPushNotificationsEnabled())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}

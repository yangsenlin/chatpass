package com.chatpass.controller.api.v1;

import com.chatpass.dto.DeviceDTO;
import com.chatpass.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {
    
    private final DeviceService deviceService;
    
    /**
     * 注册设备
     */
    @PostMapping
    public ResponseEntity<DeviceDTO> registerDevice(
            @PathVariable Long userId,
            @RequestParam String deviceType,
            @RequestParam String deviceName,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String browser,
            @RequestParam(required = false) String ipAddress) {
        
        DeviceDTO device = deviceService.registerDevice(userId, deviceType, deviceName, os, browser, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }
    
    /**
     * 获取所有设备
     */
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices(@PathVariable Long userId) {
        List<DeviceDTO> devices = deviceService.getDevicesByUser(userId);
        return ResponseEntity.ok(devices);
    }
    
    /**
     * 获取设备详情
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable Long deviceId) {
        return deviceService.getDeviceById(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取当前设备
     */
    @GetMapping("/current")
    public ResponseEntity<DeviceDTO> getCurrentDevice(@PathVariable Long userId) {
        return deviceService.getCurrentDevice(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 设置当前设备
     */
    @PostMapping("/{deviceId}/current")
    public ResponseEntity<Void> setCurrentDevice(
            @PathVariable Long userId,
            @PathVariable Long deviceId) {
        
        deviceService.setCurrentDevice(userId, deviceId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除设备
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable Long userId,
            @PathVariable Long deviceId) {
        
        deviceService.deleteDevice(userId, deviceId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 清理非活跃设备
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Integer> cleanupInactiveDevices(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int daysThreshold) {
        
        int remaining = deviceService.cleanupInactiveDevices(userId, daysThreshold);
        return ResponseEntity.ok(remaining);
    }
    
    /**
     * 更新推送通知设置
     */
    @PatchMapping("/{deviceId}/push")
    public ResponseEntity<Void> updatePushNotifications(
            @PathVariable Long deviceId,
            @RequestParam boolean enabled) {
        
        deviceService.updatePushNotifications(deviceId, enabled);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 更新设备活跃状态
     */
    @PostMapping("/{deviceId}/activity")
    public ResponseEntity<Void> updateActivity(@PathVariable Long deviceId) {
        deviceService.updateDeviceActivity(String.valueOf(deviceId));
        return ResponseEntity.ok().build();
    }
}

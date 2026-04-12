package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 设备管理实体
 * 用于记录用户的登录设备信息
 */
@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 所属用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 设备类型
     */
    @Column(name = "device_type", length = 20)
    private String deviceType; // desktop, mobile, tablet, web
    
    /**
     * 设备名称
     */
    @Column(name = "device_name", length = 100)
    private String deviceName;
    
    /**
     * 操作系统
     */
    @Column(name = "os", length = 50)
    private String os;
    
    /**
     * 浏览器/应用版本
     */
    @Column(name = "browser", length = 50)
    private String browser;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * 设备唯一标识
     */
    @Column(name = "device_id", unique = true, length = 100)
    private String deviceId;
    
    /**
     * 最后活跃时间
     */
    @Column(name = "last_active")
    private LocalDateTime lastActive;
    
    /**
     * 最后登录时间
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    /**
     * 是否为当前设备
     */
    @Column(name = "is_current")
    private Boolean isCurrent = false;
    
    /**
     * 是否推送通知开启
     */
    @Column(name = "push_notifications_enabled")
    private Boolean pushNotificationsEnabled = false;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
        lastLogin = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

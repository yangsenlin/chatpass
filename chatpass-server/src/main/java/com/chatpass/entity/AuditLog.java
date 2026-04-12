package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 * 用于记录系统操作日志
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 操作用户ID
     */
    @Column(name = "actor_id", nullable = false)
    private Long actorId;
    
    /**
     * 操作类型
     */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // user_created, message_sent, stream_created, etc.
    
    /**
     * 操作时间
     */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
    
    /**
     * 所属组织ID
     */
    @Column(name = "realm_id")
    private Long realmId;
    
    /**
     * 受影响对象类型
     */
    @Column(name = "object_type", length = 50)
    private String objectType; // user, message, stream, realm, etc.
    
    /**
     * 受影响对象ID
     */
    @Column(name = "object_id")
    private Long objectId;
    
    /**
     * 操作详情（JSON格式）
     */
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * User-Agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 操作结果
     */
    @Column(name = "result", length = 20)
    @Builder.Default
    private String result = "success"; // success, failure
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }
}

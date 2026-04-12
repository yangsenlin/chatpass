package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * CustomProfileFieldValue 实体 - 用户自定义字段值
 * 
 * 对应 Zulip CustomProfileFieldValue model
 */
@Entity
@Table(name = "custom_profile_field_values", 
    indexes = {
        @Index(name = "idx_field_value_user", columnList = "user_profile_id"),
        @Index(name = "idx_field_value_field", columnList = "field_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_field", columnNames = {"user_profile_id", "field_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomProfileFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private CustomProfileField field;

    // 字段值
    @Column(name = "value", columnDefinition = "text")
    private String value;

    // 渲染后的值（Markdown渲染）
    @Column(name = "rendered_value", columnDefinition = "text")
    private String renderedValue;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
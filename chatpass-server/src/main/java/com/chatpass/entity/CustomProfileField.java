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
 * CustomProfileField 实体 - 自定义用户字段定义
 * 
 * 对应 Zulip CustomProfileField model
 */
@Entity
@Table(name = "custom_profile_fields", indexes = {
    @Index(name = "idx_custom_field_realm", columnList = "realm_id"),
    @Index(name = "idx_custom_field_order", columnList = "realm_id, order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomProfileField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // 字段名称
    @Column(name = "name", nullable = false, length = 40)
    private String name;

    // 字段提示
    @Column(name = "hint", length = 80)
    @Builder.Default
    private String hint = "";

    // 字段类型
    @Column(name = "field_type", nullable = false)
    private Integer fieldType;

    // 字段类型常量
    public static final int TYPE_SHORT_TEXT = 1;
    public static final int TYPE_LONG_TEXT = 2;
    public static final int TYPE_SELECT = 3;
    public static final int TYPE_EXTERNAL_ACCOUNT = 4;
    public static final int TYPE_USER = 5;
    public static final int TYPE_DATE = 6;

    // 显示顺序
    @Column(name = "order")
    @Builder.Default
    private Integer order = 0;

    // 是否在摘要中显示
    @Column(name = "display_in_profile_summary")
    @Builder.Default
    private Boolean displayInProfileSummary = false;

    // 是否必填
    @Column(name = "required")
    @Builder.Default
    private Boolean required = false;

    // 是否用于用户匹配
    @Column(name = "use_for_user_matching")
    @Builder.Default
    private Boolean useForUserMatching = false;

    // 用户是否可编辑
    @Column(name = "editable_by_user")
    @Builder.Default
    private Boolean editableByUser = true;

    // 字段数据（JSON格式，用于SELECT类型）
    @Column(name = "field_data", columnDefinition = "text")
    @Builder.Default
    private String fieldData = "";

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * 获取字段类型名称
     */
    public String getFieldTypeName() {
        switch (fieldType) {
            case TYPE_SHORT_TEXT: return "短文本";
            case TYPE_LONG_TEXT: return "长文本";
            case TYPE_SELECT: return "选择";
            case TYPE_EXTERNAL_ACCOUNT: return "外部账号";
            case TYPE_USER: return "用户";
            case TYPE_DATE: return "日期";
            default: return "未知";
        }
    }
}
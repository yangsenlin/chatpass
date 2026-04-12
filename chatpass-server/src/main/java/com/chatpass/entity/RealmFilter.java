package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * RealmFilter 实体 - 链接转换器
 * 
 * 组织级别的自动链接转换规则，用于将特定文本模式转换为链接
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "realm_filters", uniqueConstraints = {
    @UniqueConstraint(name = "uniq_realm_pattern", columnNames = {"realm_id", "pattern"})
})
public class RealmFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属组织
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    /**
     * 正则表达式模式
     */
    @Column(nullable = false)
    private String pattern;

    /**
     * URL 模板，可以使用捕获组变量
     * 例如: https://example.com/{id}
     */
    @Column(name = "url_template", nullable = false)
    private String urlTemplate;

    /**
     * 示例输入文本
     */
    @Column(name = "example_input")
    private String exampleInput;

    /**
     * 反向模板（用于从链接还原文本）
     */
    @Column(name = "reverse_template")
    private String reverseTemplate;

    /**
     * 备用 URL 模板列表
     */
    @Column(name = "alternative_url_templates", columnDefinition = "JSON")
    private String alternativeUrlTemplates;

    /**
     * 处理顺序（数字小的先处理）
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer order = 0;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
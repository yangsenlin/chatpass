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
 * Attachment 实体 - 消息附件
 * 
 * 支持文件、图片、视频等附件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attachments", indexes = {
    @Index(name = "idx_attachments_message", columnList = "message_id"),
    @Index(name = "idx_attachments_owner", columnList = "owner_id")
})
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserProfile owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // 文件信息
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    // 文件类型
    @Column(name = "file_type")
    @Builder.Default
    private Integer fileType = TYPE_FILE;

    // 图片专属
    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    // 访问路径
    @Column(name = "url")
    private String url;

    @Column(name = "path_id")
    private String pathId;

    // 状态
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "is_ready")
    @Builder.Default
    private Boolean isReady = true;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // 文件类型常量
    public static final int TYPE_FILE = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_AUDIO = 4;
    public static final int TYPE_PDF = 5;

    public boolean isImage() {
        return fileType != null && fileType == TYPE_IMAGE;
    }

    public boolean isVideo() {
        return fileType != null && fileType == TYPE_VIDEO;
    }
}
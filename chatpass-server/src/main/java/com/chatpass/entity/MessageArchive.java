package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MessageArchive 实体
 * 消息归档记录
 */
@Entity
@Table(name = "message_archives")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "realm_id", nullable = false)
    private Long realmId;

    @Column(name = "archive_id", nullable = false, unique = true, length = 50)
    private String archiveId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "compressed_content", columnDefinition = "TEXT")
    private String compressedContent;

    @Column(name = "storage_path", length = 200)
    private String storagePath;

    @Column(name = "storage_type", length = 20)
    private String storageType;

    @Column(name = "archive_reason", length = 30)
    private String archiveReason;

    @Column(name = "archive_date", nullable = false)
    private LocalDateTime archiveDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "restore_count")
    @Builder.Default
    private Integer restoreCount = 0;

    @Column(name = "last_restored")
    private LocalDateTime lastRestored;

    // Storage types
    public static final String STORAGE_DATABASE = "DATABASE";
    public static final String STORAGE_FILE = "FILE";
    public static final String STORAGE_S3 = "S3";
    
    // Archive reasons
    public static final String REASON_EXPIRED = "EXPIRED";
    public static final String REASON_DELETED = "DELETED";
    public static final String REASON_USER_REQUEST = "USER_REQUEST";
    public static final String REASON_POLICY = "POLICY";
}
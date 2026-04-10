package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MessageTranslation 实体
 * 消息翻译记录
 */
@Entity
@Table(name = "message_translations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "source_language", nullable = false, length = 10)
    private String sourceLanguage;

    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage;

    @Column(name = "original_text", columnDefinition = "TEXT")
    private String originalText;

    @Column(name = "translated_text", columnDefinition = "TEXT")
    private String translatedText;

    @Column(name = "translation_provider", length = 30)
    private String translationProvider;

    @Column(name = "translation_status", length = 20)
    private String translationStatus;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "translator_id")
    private Long translatorId;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Providers
    public static final String PROVIDER_GOOGLE = "GOOGLE";
    public static final String PROVIDER_DEEP_L = "DEEP_L";
    public static final String PROVIDER_AZURE = "AZURE";
    public static final String PROVIDER_MANUAL = "MANUAL";
    
    // Status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
}
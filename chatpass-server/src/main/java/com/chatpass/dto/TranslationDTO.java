package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TranslationDTO
 */
public class TranslationDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranslationResponse {
        private Long id;
        private Long messageId;
        private String sourceLanguage;
        private String targetLanguage;
        private String originalText;
        private String translatedText;
        private String translationProvider;
        private String translationStatus;
        private Double confidenceScore;
        private Long translatorId;
        private String dateCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranslateRequest {
        private Long messageId;
        private String sourceLanguage;
        private String targetLanguage;
        private String originalText;
        private String provider;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManualTranslateRequest {
        private Long messageId;
        private String sourceLanguage;
        private String targetLanguage;
        private String originalText;
        private String translatedText;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LanguageStats {
        private String language;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranslationListResponse {
        private List<TranslationResponse> translations;
        private Long total;
    }
}
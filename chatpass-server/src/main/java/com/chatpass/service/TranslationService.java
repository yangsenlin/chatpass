package com.chatpass.service;

import com.chatpass.entity.MessageTranslation;
import com.chatpass.repository.MessageTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TranslationService
 * 消息翻译服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    private final MessageTranslationRepository translationRepository;

    /**
     * 创建翻译请求
     */
    @Transactional
    public MessageTranslation createTranslation(Long messageId, String sourceLanguage,
                                                 String targetLanguage, String originalText,
                                                 String provider) {
        MessageTranslation translation = MessageTranslation.builder()
                .messageId(messageId)
                .sourceLanguage(sourceLanguage)
                .targetLanguage(targetLanguage)
                .originalText(originalText)
                .translationProvider(provider)
                .translationStatus(MessageTranslation.STATUS_PENDING)
                .dateCreated(LocalDateTime.now())
                .build();

        return translationRepository.save(translation);
    }

    /**
     * 获取翻译
     */
    public Optional<MessageTranslation> getTranslation(Long translationId) {
        return translationRepository.findById(translationId);
    }

    /**
     * 获取消息的所有翻译
     */
    public List<MessageTranslation> getMessageTranslations(Long messageId) {
        return translationRepository.findByMessageId(messageId);
    }

    /**
     * 获取指定语言的翻译
     */
    public Optional<MessageTranslation> getTranslationByLanguage(Long messageId, String targetLanguage) {
        return translationRepository.findByMessageIdAndTargetLanguage(messageId, targetLanguage);
    }

    /**
     * 获取已完成的翻译
     */
    public List<MessageTranslation> getCompletedTranslations(Long messageId) {
        return translationRepository.findCompletedTranslations(messageId);
    }

    /**
     * 获取待处理翻译
     */
    public List<MessageTranslation> getPendingTranslations() {
        return translationRepository.findByTranslationStatus(MessageTranslation.STATUS_PENDING);
    }

    /**
     * 完成翻译
     */
    @Transactional
    public MessageTranslation completeTranslation(Long translationId, String translatedText,
                                                   Double confidenceScore) {
        MessageTranslation translation = translationRepository.findById(translationId)
                .orElseThrow(() -> new IllegalArgumentException("翻译不存在"));

        translation.setTranslatedText(translatedText);
        translation.setConfidenceScore(confidenceScore);
        translation.setTranslationStatus(MessageTranslation.STATUS_COMPLETED);
        translation.setLastUpdated(LocalDateTime.now());

        return translationRepository.save(translation);
    }

    /**
     * 标记翻译失败
     */
    @Transactional
    public MessageTranslation markTranslationFailed(Long translationId) {
        MessageTranslation translation = translationRepository.findById(translationId)
                .orElseThrow(() -> new IllegalArgumentException("翻译不存在"));

        translation.setTranslationStatus(MessageTranslation.STATUS_FAILED);
        translation.setLastUpdated(LocalDateTime.now());

        return translationRepository.save(translation);
    }

    /**
     * 手动翻译
     */
    @Transactional
    public MessageTranslation manualTranslation(Long messageId, String sourceLanguage,
                                                 String targetLanguage, String originalText,
                                                 String translatedText, Long translatorId) {
        MessageTranslation translation = MessageTranslation.builder()
                .messageId(messageId)
                .sourceLanguage(sourceLanguage)
                .targetLanguage(targetLanguage)
                .originalText(originalText)
                .translatedText(translatedText)
                .translationProvider(MessageTranslation.PROVIDER_MANUAL)
                .translationStatus(MessageTranslation.STATUS_COMPLETED)
                .translatorId(translatorId)
                .dateCreated(LocalDateTime.now())
                .build();

        return translationRepository.save(translation);
    }

    /**
     * 翻译消息（模拟）
     */
    @Transactional
    public MessageTranslation translate(Long translationId) {
        MessageTranslation translation = translationRepository.findById(translationId)
                .orElseThrow(() -> new IllegalArgumentException("翻译不存在"));

        try {
            log.info("Translating message {} from {} to {} using {}",
                    translation.getMessageId(), translation.getSourceLanguage(),
                    translation.getTargetLanguage(), translation.getTranslationProvider());

            // 实际实现需要调用翻译 API
            // Google: Google Cloud Translation API
            // DeepL: DeepL API
            // Azure: Azure Translator

            // 模拟翻译结果
            String translatedText = "[Translated] " + translation.getOriginalText();
            Double confidence = 0.95;

            return completeTranslation(translationId, translatedText, confidence);
        } catch (Exception e) {
            log.error("Translation failed: {}", e.getMessage());
            return markTranslationFailed(translationId);
        }
    }

    /**
     * 统计翻译使用
     */
    public Long countByProvider(String provider) {
        return translationRepository.countByProvider(provider);
    }

    /**
     * 统计语言分布
     */
    public List<Object[]> countByTargetLanguage() {
        return translationRepository.countByTargetLanguage();
    }
}
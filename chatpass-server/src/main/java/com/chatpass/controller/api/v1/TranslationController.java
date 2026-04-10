package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.TranslationDTO;
import com.chatpass.entity.MessageTranslation;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translation 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Translation", description = "消息翻译 API")
public class TranslationController {

    private final TranslationService translationService;
    private final SecurityUtil securityUtil;

    @PostMapping("/translations")
    @Operation(summary = "创建翻译请求")
    public ResponseEntity<ApiResponse<TranslationDTO.TranslationResponse>> createTranslation(
            @RequestBody TranslationDTO.TranslateRequest request) {
        MessageTranslation translation = translationService.createTranslation(
                request.getMessageId(), request.getSourceLanguage(),
                request.getTargetLanguage(), request.getOriginalText(), request.getProvider());

        return ResponseEntity.ok(ApiResponse.success(toTranslationResponse(translation)));
    }

    @GetMapping("/translations")
    @Operation(summary = "获取待处理翻译")
    public ResponseEntity<ApiResponse<List<TranslationDTO.TranslationResponse>>> getPendingTranslations() {
        List<MessageTranslation> translations = translationService.getPendingTranslations();

        List<TranslationDTO.TranslationResponse> response = translations.stream()
                .map(this::toTranslationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/translations/{translationId}")
    @Operation(summary = "获取翻译详情")
    public ResponseEntity<ApiResponse<TranslationDTO.TranslationResponse>> getTranslation(
            @PathVariable Long translationId) {
        MessageTranslation translation = translationService.getTranslation(translationId)
                .orElseThrow(() -> new IllegalArgumentException("翻译不存在"));

        return ResponseEntity.ok(ApiResponse.success(toTranslationResponse(translation)));
    }

    @GetMapping("/messages/{messageId}/translations")
    @Operation(summary = "获取消息的所有翻译")
    public ResponseEntity<ApiResponse<List<TranslationDTO.TranslationResponse>>> getMessageTranslations(
            @PathVariable Long messageId) {
        List<MessageTranslation> translations = translationService.getMessageTranslations(messageId);

        List<TranslationDTO.TranslationResponse> response = translations.stream()
                .map(this::toTranslationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}/translations/{language}")
    @Operation(summary = "获取消息指定语言翻译")
    public ResponseEntity<ApiResponse<TranslationDTO.TranslationResponse>> getTranslationByLanguage(
            @PathVariable Long messageId, @PathVariable String language) {
        MessageTranslation translation = translationService.getTranslationByLanguage(messageId, language)
                .orElseThrow(() -> new IllegalArgumentException("翻译不存在"));

        return ResponseEntity.ok(ApiResponse.success(toTranslationResponse(translation)));
    }

    @PostMapping("/translations/{translationId}/execute")
    @Operation(summary = "执行翻译")
    public ResponseEntity<ApiResponse<TranslationDTO.TranslationResponse>> executeTranslation(
            @PathVariable Long translationId) {
        MessageTranslation translation = translationService.translate(translationId);

        return ResponseEntity.ok(ApiResponse.success(toTranslationResponse(translation)));
    }

    @PostMapping("/translations/manual")
    @Operation(summary = "手动翻译")
    public ResponseEntity<ApiResponse<TranslationDTO.TranslationResponse>> manualTranslation(
            @RequestBody TranslationDTO.ManualTranslateRequest request) {
        Long userId = securityUtil.getCurrentUserId();

        MessageTranslation translation = translationService.manualTranslation(
                request.getMessageId(), request.getSourceLanguage(),
                request.getTargetLanguage(), request.getOriginalText(),
                request.getTranslatedText(), userId);

        return ResponseEntity.ok(ApiResponse.success(toTranslationResponse(translation)));
    }

    @GetMapping("/translations/stats/languages")
    @Operation(summary = "统计语言分布")
    public ResponseEntity<ApiResponse<List<TranslationDTO.LanguageStats>>> getLanguageStats() {
        List<Object[]> stats = translationService.countByTargetLanguage();

        List<TranslationDTO.LanguageStats> response = stats.stream()
                .map(stat -> TranslationDTO.LanguageStats.builder()
                        .language((String) stat[0])
                        .count((Long) stat[1])
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/translations/stats/providers")
    @Operation(summary = "统计翻译服务")
    public ResponseEntity<ApiResponse<Long>> getProviderStats(@RequestParam String provider) {
        Long count = translationService.countByProvider(provider);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    private TranslationDTO.TranslationResponse toTranslationResponse(MessageTranslation translation) {
        return TranslationDTO.TranslationResponse.builder()
                .id(translation.getId())
                .messageId(translation.getMessageId())
                .sourceLanguage(translation.getSourceLanguage())
                .targetLanguage(translation.getTargetLanguage())
                .originalText(translation.getOriginalText())
                .translatedText(translation.getTranslatedText())
                .translationProvider(translation.getTranslationProvider())
                .translationStatus(translation.getTranslationStatus())
                .confidenceScore(translation.getConfidenceScore())
                .translatorId(translation.getTranslatorId())
                .dateCreated(translation.getDateCreated() != null ? translation.getDateCreated().toString() : null)
                .build();
    }
}
package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.CustomEmojiDTO;
import com.chatpass.entity.CustomEmoji;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.CustomEmojiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CustomEmoji 控制器
 * 
 * 自定义表情 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Custom Emojis", description = "自定义表情 API")
public class CustomEmojiController {

    private final CustomEmojiService emojiService;
    private final SecurityUtil securityUtil;

    @GetMapping("/emojis")
    @Operation(summary = "获取所有表情")
    public ResponseEntity<ApiResponse<List<CustomEmojiDTO.EmojiResponse>>> getAllEmojis() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<CustomEmoji> emojis = emojiService.getAllEmojis(realmId);
        
        List<CustomEmojiDTO.EmojiResponse> response = emojis.stream()
                .map(emojiService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/emojis/{emojiId}")
    @Operation(summary = "获取表情详情")
    public ResponseEntity<ApiResponse<CustomEmojiDTO.EmojiResponse>> getEmoji(
            @PathVariable Long emojiId) {
        CustomEmoji emoji = emojiService.getEmoji(emojiId)
                .orElseThrow(() -> new IllegalArgumentException("表情不存在"));
        
        return ResponseEntity.ok(ApiResponse.success(emojiService.toResponse(emoji)));
    }

    @GetMapping("/emojis/name/{name}")
    @Operation(summary = "根据名称获取表情")
    public ResponseEntity<ApiResponse<CustomEmojiDTO.EmojiResponse>> getEmojiByName(
            @PathVariable String name) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        CustomEmoji emoji = emojiService.getEmojiByName(realmId, name)
                .orElseThrow(() -> new IllegalArgumentException("表情不存在"));
        
        return ResponseEntity.ok(ApiResponse.success(emojiService.toResponse(emoji)));
    }

    @PostMapping("/emojis")
    @Operation(summary = "创建自定义表情")
    public ResponseEntity<ApiResponse<CustomEmojiDTO.EmojiResponse>> createEmoji(
            @RequestBody CustomEmojiDTO.CreateRequest request) {
        Long realmId = securityUtil.getCurrentRealmId();
        Long userId = securityUtil.getCurrentUserId();
        
        CustomEmoji emoji = emojiService.createEmoji(realmId, userId, request);
        
        return ResponseEntity.ok(ApiResponse.success(emojiService.toResponse(emoji)));
    }

    @PutMapping("/emojis/{emojiId}")
    @Operation(summary = "更新表情")
    public ResponseEntity<ApiResponse<CustomEmojiDTO.EmojiResponse>> updateEmoji(
            @PathVariable Long emojiId,
            @RequestBody CustomEmojiDTO.UpdateRequest request) {
        CustomEmoji emoji = emojiService.updateEmoji(emojiId, request);
        
        return ResponseEntity.ok(ApiResponse.success(emojiService.toResponse(emoji)));
    }

    @DeleteMapping("/emojis/{emojiId}")
    @Operation(summary = "删除表情")
    public ResponseEntity<ApiResponse<Void>> deleteEmoji(@PathVariable Long emojiId) {
        emojiService.deleteEmoji(emojiId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/emojis/search")
    @Operation(summary = "搜索表情")
    public ResponseEntity<ApiResponse<List<CustomEmojiDTO.EmojiResponse>>> searchEmojis(
            @RequestParam String query) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<CustomEmoji> emojis = emojiService.searchEmojis(realmId, query);
        
        List<CustomEmojiDTO.EmojiResponse> response = emojis.stream()
                .map(emojiService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/me/emojis")
    @Operation(summary = "获取我创建的表情")
    public ResponseEntity<ApiResponse<List<CustomEmojiDTO.EmojiResponse>>> getMyEmojis() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<CustomEmoji> emojis = emojiService.getUserEmojis(userId);
        
        List<CustomEmojiDTO.EmojiResponse> response = emojis.stream()
                .map(emojiService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/emojis/count")
    @Operation(summary = "统计表情数量")
    public ResponseEntity<ApiResponse<Long>> getEmojiCount() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        Long count = emojiService.getEmojiCount(realmId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
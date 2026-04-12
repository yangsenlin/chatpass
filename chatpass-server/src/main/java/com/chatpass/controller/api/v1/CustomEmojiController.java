package com.chatpass.controller.api.v1;

import com.chatpass.dto.CustomEmojiDTO;
import com.chatpass.service.CustomEmojiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 自定义表情控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CustomEmojiController {
    
    private final CustomEmojiService emojiService;
    
    /**
     * 上传表情
     */
    @PostMapping("/realm/{realmId}/emojis")
    public ResponseEntity<CustomEmojiDTO> uploadEmoji(
            @PathVariable Long realmId,
            @RequestParam String name,
            @RequestParam(required = false) String aliases,
            @RequestParam Long authorId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        CustomEmojiDTO emoji = emojiService.uploadEmoji(realmId, name, aliases, authorId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(emoji);
    }
    
    /**
     * 获取组织的所有表情
     */
    @GetMapping("/realm/{realmId}/emojis")
    public ResponseEntity<List<CustomEmojiDTO>> getRealmEmojis(@PathVariable Long realmId) {
        List<CustomEmojiDTO> emojis = emojiService.getEmojisByRealm(realmId);
        return ResponseEntity.ok(emojis);
    }
    
    /**
     * 获取表情详情
     */
    @GetMapping("/emojis/{emojiId}")
    public ResponseEntity<CustomEmojiDTO> getEmoji(@PathVariable Long emojiId) {
        return emojiService.getEmojiById(emojiId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据名称获取表情
     */
    @GetMapping("/realm/{realmId}/emojis/name/{name}")
    public ResponseEntity<CustomEmojiDTO> getEmojiByName(
            @PathVariable Long realmId,
            @PathVariable String name) {
        
        return emojiService.getEmojiByName(realmId, name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 搜索表情
     */
    @GetMapping("/realm/{realmId}/emojis/search")
    public ResponseEntity<List<CustomEmojiDTO>> searchEmojis(
            @PathVariable Long realmId,
            @RequestParam String keyword) {
        
        List<CustomEmojiDTO> emojis = emojiService.searchEmojis(realmId, keyword);
        return ResponseEntity.ok(emojis);
    }
    
    /**
     * 更新表情别名
     */
    @PatchMapping("/emojis/{emojiId}/aliases")
    public ResponseEntity<CustomEmojiDTO> updateAliases(
            @PathVariable Long emojiId,
            @RequestParam String aliases) {
        
        CustomEmojiDTO emoji = emojiService.updateAliases(emojiId, aliases);
        return ResponseEntity.ok(emoji);
    }
    
    /**
     * 禁用表情
     */
    @PostMapping("/emojis/{emojiId}/deactivate")
    public ResponseEntity<Void> deactivateEmoji(@PathVariable Long emojiId) {
        emojiService.deactivateEmoji(emojiId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 恢复表情
     */
    @PostMapping("/emojis/{emojiId}/reactivate")
    public ResponseEntity<Void> reactivateEmoji(@PathVariable Long emojiId) {
        emojiService.reactivateEmoji(emojiId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 记录使用
     */
    @PostMapping("/emojis/{emojiId}/use")
    public ResponseEntity<Void> recordUsage(@PathVariable Long emojiId) {
        emojiService.recordUsage(emojiId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取用户创建的表情
     */
    @GetMapping("/users/{authorId}/emojis")
    public ResponseEntity<List<CustomEmojiDTO>> getUserEmojis(@PathVariable Long authorId) {
        List<CustomEmojiDTO> emojis = emojiService.getEmojisByAuthor(authorId);
        return ResponseEntity.ok(emojis);
    }
}

package com.chatpass.controller.api.v1;

import com.chatpass.dto.PinnedMessageDTO;
import com.chatpass.service.PinnedMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 固定消息控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PinnedMessageController {
    
    private final PinnedMessageService pinnedMessageService;
    
    /**
     * 固定消息
     */
    @PostMapping("/messages/{messageId}/pin")
    public ResponseEntity<PinnedMessageDTO> pinMessage(
            @PathVariable Long messageId,
            @RequestParam(required = false) Long streamId,
            @RequestParam(required = false) String topic,
            @RequestParam Long realmId,
            @RequestParam Long pinnedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt) {
        
        PinnedMessageDTO pinned = pinnedMessageService.pinMessage(messageId, streamId, topic, realmId, pinnedBy, expiresAt);
        return ResponseEntity.status(HttpStatus.CREATED).body(pinned);
    }
    
    /**
     * 取消固定
     */
    @DeleteMapping("/messages/{messageId}/pin")
    public ResponseEntity<Void> unpinMessage(@PathVariable Long messageId) {
        pinnedMessageService.unpinMessage(messageId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取组织的固定消息
     */
    @GetMapping("/realm/{realmId}/pinned_messages")
    public ResponseEntity<List<PinnedMessageDTO>> getRealmPinnedMessages(@PathVariable Long realmId) {
        List<PinnedMessageDTO> pinned = pinnedMessageService.getRealmPinnedMessages(realmId);
        return ResponseEntity.ok(pinned);
    }
    
    /**
     * 获取Stream的固定消息
     */
    @GetMapping("/streams/{streamId}/pinned_messages")
    public ResponseEntity<List<PinnedMessageDTO>> getStreamPinnedMessages(@PathVariable Long streamId) {
        List<PinnedMessageDTO> pinned = pinnedMessageService.getStreamPinnedMessages(streamId);
        return ResponseEntity.ok(pinned);
    }
    
    /**
     * 获取Topic的固定消息
     */
    @GetMapping("/streams/{streamId}/topics/{topic}/pinned_messages")
    public ResponseEntity<List<PinnedMessageDTO>> getTopicPinnedMessages(
            @PathVariable Long streamId,
            @PathVariable String topic) {
        
        List<PinnedMessageDTO> pinned = pinnedMessageService.getTopicPinnedMessages(streamId, topic);
        return ResponseEntity.ok(pinned);
    }
    
    /**
     * 获取固定消息详情
     */
    @GetMapping("/pinned_messages/{pinnedId}")
    public ResponseEntity<PinnedMessageDTO> getPinnedMessage(@PathVariable Long pinnedId) {
        return pinnedMessageService.getPinnedMessageById(pinnedId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新排序
     */
    @PatchMapping("/pinned_messages/{pinnedId}/sort")
    public ResponseEntity<Void> updateSortOrder(
            @PathVariable Long pinnedId,
            @RequestParam Integer sortOrder) {
        
        pinnedMessageService.updateSortOrder(pinnedId, sortOrder);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 设置过期时间
     */
    @PatchMapping("/pinned_messages/{pinnedId}/expiry")
    public ResponseEntity<Void> setExpiry(
            @PathVariable Long pinnedId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt) {
        
        pinnedMessageService.setExpiry(pinnedId, expiresAt);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 检查消息是否已固定
     */
    @GetMapping("/messages/{messageId}/pinned")
    public ResponseEntity<Boolean> isMessagePinned(@PathVariable Long messageId) {
        boolean pinned = pinnedMessageService.isMessagePinned(messageId);
        return ResponseEntity.ok(pinned);
    }
    
    /**
     * 获取用户的固定消息
     */
    @GetMapping("/users/{pinnedBy}/pinned_messages")
    public ResponseEntity<List<PinnedMessageDTO>> getUserPinnedMessages(@PathVariable Long pinnedBy) {
        List<PinnedMessageDTO> pinned = pinnedMessageService.getUserPinnedMessages(pinnedBy);
        return ResponseEntity.ok(pinned);
    }
}

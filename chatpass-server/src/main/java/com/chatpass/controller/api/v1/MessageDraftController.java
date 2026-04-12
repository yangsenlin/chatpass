package com.chatpass.controller.api.v1;

import com.chatpass.dto.MessageDraftDTO;
import com.chatpass.service.MessageDraftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息草稿控制器
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/drafts")
@RequiredArgsConstructor
@Slf4j
public class MessageDraftController {
    
    private final MessageDraftService draftService;
    
    /**
     * 保存草稿
     */
    @PostMapping
    public ResponseEntity<MessageDraftDTO> saveDraft(
            @PathVariable Long userId,
            @RequestParam String type,
            @RequestParam(required = false) Long streamId,
            @RequestParam(required = false) String toUserIds,
            @RequestParam(required = false) String topic,
            @RequestParam String content) {
        
        MessageDraftDTO draft = draftService.saveDraft(userId, type, streamId, toUserIds, topic, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(draft);
    }
    
    /**
     * 获取所有草稿
     */
    @GetMapping
    public ResponseEntity<List<MessageDraftDTO>> getUserDrafts(@PathVariable Long userId) {
        List<MessageDraftDTO> drafts = draftService.getUserDrafts(userId);
        return ResponseEntity.ok(drafts);
    }
    
    /**
     * 获取草稿详情
     */
    @GetMapping("/{draftId}")
    public ResponseEntity<MessageDraftDTO> getDraft(@PathVariable Long draftId) {
        return draftService.getDraftById(draftId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取Stream草稿
     */
    @GetMapping("/stream")
    public ResponseEntity<MessageDraftDTO> getStreamDraft(
            @PathVariable Long userId,
            @RequestParam Long streamId,
            @RequestParam(required = false) String topic) {
        
        return draftService.getStreamDraft(userId, streamId, topic)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取私信草稿
     */
    @GetMapping("/private")
    public ResponseEntity<MessageDraftDTO> getPrivateDraft(
            @PathVariable Long userId,
            @RequestParam String toUserIds) {
        
        return draftService.getPrivateDraft(userId, toUserIds)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除草稿
     */
    @DeleteMapping("/{draftId}")
    public ResponseEntity<Void> deleteDraft(
            @PathVariable Long userId,
            @PathVariable Long draftId) {
        
        draftService.deleteDraft(userId, draftId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 清空所有草稿
     */
    @DeleteMapping
    public ResponseEntity<Void> clearDrafts(@PathVariable Long userId) {
        draftService.clearUserDrafts(userId);
        return ResponseEntity.noContent().build();
    }
}
